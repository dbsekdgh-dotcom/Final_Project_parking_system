package com.example.demo.domain.user.auth.service;

import com.example.demo.domain.shared.approval.enums.ApprovalStatus;
import com.example.demo.domain.shared.approval.enums.ApprovalType;
import com.example.demo.domain.shared.approval.repository.ApprovalRepository;
import com.example.demo.domain.shared.user.User;
import com.example.demo.domain.user.auth.dtos.request.UserLoginRequestDto;
import com.example.demo.domain.user.auth.dtos.response.UserLoginResponseDto;
import com.example.demo.global.util.admin.AdminJWTUtil;
import com.example.demo.domain.user.auth.repository.SocialAccountRepository;
import com.example.demo.domain.user.auth.repository.UserAuthRepository;
import com.example.demo.domain.shared.user.enums.Status;
import com.example.demo.global.exception.AuthException;
import com.example.demo.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserLoginService {

    private final SocialAccountRepository socialAccountRepository;
    private final UserAuthRepository userAuthRepository;
    private final PasswordEncoder passwordEncoder;
    private final AdminJWTUtil adminJWTUtil;
    private final UserVerificationService userVerificationService; // ⭐ Redis 저장을 위한 주입 추가
    private final ApprovalRepository approvalRepository;

    @Transactional // 로그인 성공 시 Redis 작업을 포함하므로 쓰기 트랜잭션 필요 시를 대비해 붙여줍니다.
    public UserLoginResponseDto userLogin(UserLoginRequestDto userLoginRequestDto) {

        String email = userLoginRequestDto.getEmail();

        // 1. 필수 입력값 검증
        if (userLoginRequestDto.getEmail() == null || userLoginRequestDto.getEmail().isBlank() ||
                userLoginRequestDto.getPassword() == null || userLoginRequestDto.getPassword().isBlank()) {
            throw new AuthException(ErrorCode.INVALID_REQUEST);
        }
        // 5회 이상 실패 시 DB 조회조차 하지 않고 바로 에러 발생
        if (userVerificationService.getLoginFailCount(email) >= 5 ) {
            log.warn("로그인 시도 횟수 초과로 인한 차단: {}", email);
            throw new AuthException(ErrorCode.TOO_MANY_LOGIN_ATTEMPTS);
        }

        // 2. 가입 여부 확인
        User user = userAuthRepository.findByEmail(userLoginRequestDto.getEmail())
                .orElseThrow(() -> {
                    userVerificationService.increaseLoginFailCount(email);
                    log.warn("가입되지 않은 이메일 로그인 시도: {}", email);
                    return new AuthException(ErrorCode.USER_NOT_FOUND);
                });

        // 3. 계정 상태 확인
        if (user.getStatus() == Status.DELETED) {
            // 탈퇴 계정은 에러가 아닌 200 + 복구 안내 코드로 응답 (콘솔 에러 제거)
            log.info("탈퇴한 계정의 로그인 시도 - 복구 안내 응답: {}", user.getEmail());
            return UserLoginResponseDto.builder()
                    .code("WITHDRAWN_ACCOUNT")
                    .email(user.getEmail())
                    .build();
        }

        if (user.getStatus() != Status.ACTIVE) {
            log.warn("비활성화 계정의 로그인 시도 차단: {}", user.getEmail());
            throw new AuthException(ErrorCode.ACCOUNT_DISABLED);
        }

        // 4. 소셜 가입자 체크 (비밀번호가 없는 경우)
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            if (socialAccountRepository.existsByUser(user)) {
                log.warn("소셜 가입자의 로컬 로그인 시도 차단: {}", user.getEmail());
                throw new AuthException(ErrorCode.SOCIAL_USER_LOGIN_ATTEMPT);
            }
        }

        // 5. 비밀번호 일치 확인
        if (!passwordEncoder.matches(userLoginRequestDto.getPassword(), user.getPassword())) {

            userVerificationService.increaseLoginFailCount(email);

            int currentFailCount = userVerificationService.getLoginFailCount(email);
            log.warn("로그인 실패 - 이메일: {}, 실패 횟수: {}", email, currentFailCount);

            throw new AuthException(ErrorCode.LOGIN_FAILED);
        }

        // 6. 로그인 성공 처리 및 토큰 생성
        try {
            // ⭐ 재발급 컨트롤러와의 규격을 맞추기 위해 role 추가
            Map<String, Object> claims = Map.of(
                    "email", user.getEmail(),
                    "name", user.getName(),
                    "userId", user.getUserId(),
                    "role", "USER"
            );

            String accessToken = adminJWTUtil.generateUserAccessToken(claims);
            String refreshToken = adminJWTUtil.generateUserRefreshToken(claims);

            // ⭐ [핵심 추가] 생성된 Refresh Token을 Redis에 저장 (6시간)
            userVerificationService.saveRefreshToken(user.getEmail(), refreshToken);

            userVerificationService.deleteLoginFailCount(email);

            log.info("로그인 성공 및 Redis RT 저장 완료: {}", user.getEmail());

            // 입주민 상태 및 호수 조회 (User 조인 + Approval 조회, 엔티티 컬럼 추가 없음)
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

            return UserLoginResponseDto.builder()
                    .userId(user.getUserId())
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .email(user.getEmail())
                    .name(user.getName())
                    .userStatus(userStatus)
                    .unitNo(unitNo)
                    .build();

        } catch (Exception e) {
            log.error("JWT 토큰 생성 중 에러 발생: ", e);
            throw new RuntimeException("로그인 처리 중 보안 토큰 발행에 실패했습니다.");
        }
    }
    @Transactional
    public void logout(String email) {
        log.info("로그아웃 처리 시작 - 이메일: {}", email);

        userVerificationService.deleteRefreshToken(email);

        log.info("로그아웃 완료 Redis 세션 제거 성공: {}", email);
    }
}