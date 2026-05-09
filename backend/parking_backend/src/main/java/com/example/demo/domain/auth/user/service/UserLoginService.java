package com.example.demo.domain.auth.user.service;

import com.example.demo.domain.approval.enums.ApprovalStatus;
import com.example.demo.domain.approval.enums.ApprovalType;
import com.example.demo.domain.approval.repository.ApprovalRepository;
import com.example.demo.domain.resident.User;
import com.example.demo.domain.auth.user.dtos.request.UserLoginRequestDto;
import com.example.demo.domain.auth.user.dtos.response.UserLoginResponseDto;
import com.example.demo.global.util.admin.AdminJWTUtil;
import com.example.demo.domain.auth.user.repository.SocialAccountRepository;
import com.example.demo.domain.auth.user.repository.UserAuthRepository;
import com.example.demo.domain.resident.enums.Status;
import com.example.demo.global.exception.AuthException;
import com.example.demo.global.exception.ErrorCode;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserLoginService {

    @Value("${cookie.domain:}")
    private String cookieDomain;

    @Value("${cookie.secure:false}")
    private boolean cookieSecure;

    private final SocialAccountRepository socialAccountRepository;
    private final UserAuthRepository userAuthRepository;
    private final PasswordEncoder passwordEncoder;
    private final AdminJWTUtil adminJWTUtil;
    private final UserVerificationService userVerificationService;
    private final ApprovalRepository approvalRepository;

    /**
     * 로그인 서비스
     * @param response 쿠키를 굽기 위해 HttpServletResponse 추가
     */
    @Transactional
    public UserLoginResponseDto userLogin(UserLoginRequestDto userLoginRequestDto, HttpServletResponse response) {

        String email = userLoginRequestDto.getEmail();

        // 1. 필수 입력값 검증 (생략 - 기존과 동일)
        if (userLoginRequestDto.getEmail() == null || userLoginRequestDto.getEmail().isBlank() ||
                userLoginRequestDto.getPassword() == null || userLoginRequestDto.getPassword().isBlank()) {
            throw new AuthException(ErrorCode.INVALID_REQUEST);
        }

        // 로그인 실패 횟수 차단 로직 (기존 유지)
        if (userVerificationService.getLoginFailCount(email) >= 5 ) {
            log.warn("로그인 시도 횟수 초과로 인한 차단: {}", email);
            throw new AuthException(ErrorCode.TOO_MANY_LOGIN_ATTEMPTS);
        }

        // 2. 가입 여부 확인
        User user = userAuthRepository.findByEmail(userLoginRequestDto.getEmail())
                .orElseThrow(() -> {
                    userVerificationService.increaseLoginFailCount(email);
                    return new AuthException(ErrorCode.LOGIN_EMAIL_NOT_FOUND);
                });

        // 3. 계정 상태 확인 (탈퇴/비활성화 체크 - 기존 유지)
        if (user.getStatus() == Status.DELETED) {
            return UserLoginResponseDto.builder()
                    .code("WITHDRAWN_ACCOUNT")
                    .email(user.getEmail())
                    .build();
        }

        // 5. 비밀번호 일치 확인 (기존 유지)
        if (!passwordEncoder.matches(userLoginRequestDto.getPassword(), user.getPassword())) {
            userVerificationService.increaseLoginFailCount(email);
            throw new AuthException(ErrorCode.LOGIN_FAILED);
        }

        // 6. 로그인 성공 처리 및 쿠키 발급
        try {
            Map<String, Object> claims = Map.of(
                    "email", user.getEmail(),
                    "name", user.getName(),
                    "userId", user.getUserId(),
                    "role", "USER"
            );

            // 토큰 생성
            String accessToken = adminJWTUtil.generateUserAccessToken(claims);
            String refreshToken = adminJWTUtil.generateUserRefreshToken(claims);

            // Redis에 Refresh Token 저장
            userVerificationService.saveRefreshToken(user.getEmail(), refreshToken);
            userVerificationService.deleteLoginFailCount(email);

            ResponseCookie accessCookie = buildCookie("accessToken", accessToken, -1);
            ResponseCookie refreshCookie = buildCookie("refreshToken", refreshToken, -1);

            // 응답 헤더에 쿠키 추가
            response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
            response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

            log.info("로그인 성공 및 HttpOnly 쿠키 발급 완료: {}", user.getEmail());

            // 7. 입주민 상태 조회 (기존 유지)
            String userStatus;
            Integer unitNo = null;
            if (user.getHousehold() != null) {
                userStatus = "RESIDENT";
                unitNo = user.getHousehold().getUnitNo();
            } else {
                boolean hasPending = approvalRepository.existsByRequestUserIdAndApprovalTypeAndStatus(
                        user, ApprovalType.RESIDENT, ApprovalStatus.PENDING);
                userStatus = hasPending ? "PENDING" : "NONE";
            }

            // DTO 리턴 (accessToken, refreshToken 필드는 이제 제외됨)
            return UserLoginResponseDto.builder()
                    .userId(user.getUserId())
                    .email(user.getEmail())
                    .name(user.getName())
                    .userStatus(userStatus)
                    .unitNo(unitNo)
                    .build();

        } catch (Exception e) {
            log.error("로그인 처리 중 에러: ", e);
            throw new RuntimeException("인증 토큰 발행에 실패했습니다.");
        }
    }

    /**
     * 로그아웃 서비스
     * @param response 쿠키 삭제를 위해 추가
     */
    @Transactional
    public void logout(String email, HttpServletResponse response) {
        log.info("로그아웃 처리 시작 - 이메일: {}", email);

        // 1. Redis에서 RT 제거
        userVerificationService.deleteRefreshToken(email);

        // 2. 브라우저 쿠키 삭제 (Max-Age를 0으로 설정)
        ResponseCookie deleteAccess  = buildCookie("accessToken",  "", 0);
        ResponseCookie deleteRefresh = buildCookie("refreshToken", "", 0);

        response.addHeader(HttpHeaders.SET_COOKIE, deleteAccess.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, deleteRefresh.toString());

        log.info("로그아웃 완료 - Redis 및 쿠키 제거 성공: {}", email);
    }

    private ResponseCookie buildCookie(String name, String value, long maxAge) {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(name, value)
                .path("/")
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Lax");
        if (cookieDomain != null && !cookieDomain.isBlank()) builder.domain(cookieDomain);
        if (maxAge >= 0) builder.maxAge(maxAge);
        return builder.build();
    }
}