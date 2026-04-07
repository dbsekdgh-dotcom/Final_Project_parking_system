package com.example.demo.domain.user.auth.service;

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
@Transactional(readOnly = true) // 기본적으로 읽기 전용 트랜잭션 적용
public class UserLoginService {

    private final SocialAccountRepository socialAccountRepository;
    private final UserAuthRepository userAuthRepository;
    private final PasswordEncoder passwordEncoder;
    private final AdminJWTUtil adminJWTUtil;

    public UserLoginResponseDto userLogin(UserLoginRequestDto userLoginRequestDto) {

        // 1. 필수 입력값 검증
        if (userLoginRequestDto.getEmail() == null || userLoginRequestDto.getEmail().isBlank() ||
                userLoginRequestDto.getPassword() == null || userLoginRequestDto.getPassword().isBlank()) {
            throw new AuthException(ErrorCode.INVALID_REQUEST);
        }

        // 2. 가입 여부 확인
        User user = userAuthRepository.findByEmail(userLoginRequestDto.getEmail())
                .orElseThrow(() -> new AuthException(ErrorCode.USER_NOT_FOUND));

        // 3. 계정 상태 확인 (탈퇴 유저 여부를 먼저 체크하는 것이 효율적입니다)
        if (user.getStatus() == Status.DELETED) {
            log.warn("탈퇴한 계정의 로그인 시도 차단: {}", user.getEmail());
            throw new AuthException(ErrorCode.WITHDRAWN_ACCOUNT);
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
            throw new AuthException(ErrorCode.LOGIN_FAILED);
        }

        // 6. 로그인 성공 처리 및 토큰 생성
        try {
            Map<String, Object> claims = Map.of(
                    "email", user.getEmail(),
                    "name", user.getName(),
                    "userId", user.getUserId() // 토큰에 ID를 담아두면 탈퇴/수정 시 편리합니다
            );

            String accessToken = adminJWTUtil.generateUserAccessToken(claims);
            String refreshToken = adminJWTUtil.generateUserRefreshToken(claims);

            log.info("로그인 성공: {}", user.getEmail());

            return UserLoginResponseDto.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .email(user.getEmail())
                    .name(user.getName())
                    .build();

        } catch (Exception e) {
            log.error("JWT 토큰 생성 중 에러 발생: ", e);
            throw new RuntimeException("로그인 처리 중 보안 토큰 발행에 실패했습니다.");
        }
    }
}