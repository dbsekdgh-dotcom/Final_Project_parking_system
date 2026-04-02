package com.example.demo.domain.user.auth.service;

import com.example.demo.domain.shared.user.User;
import com.example.demo.domain.user.auth.dtos.request.UserLoginRequestDto;
import com.example.demo.domain.user.auth.dtos.response.UserLoginResponseDto;
import com.example.demo.domain.user.auth.jwt.JWTUtil;
import com.example.demo.domain.user.auth.repository.UserAuthRepository;
import com.example.demo.domain.shared.user.enums.Status; // Status 임포트 확인
import com.example.demo.global.exception.AuthException; // 내 커스텀 예외
import com.example.demo.global.exception.ErrorCode;     // 내 에러 코드
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

    private final UserAuthRepository userAuthRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTUtil jwtUtil;

    public UserLoginResponseDto userLogin(UserLoginRequestDto userLoginRequestDto) {

        // 1. 필수 입력값 검증 (INVALID_REQUEST 활용)
        if (userLoginRequestDto.getEmail() == null || userLoginRequestDto.getEmail().isBlank() ||
                userLoginRequestDto.getPassword() == null || userLoginRequestDto.getPassword().isBlank()) {
            throw new AuthException(ErrorCode.INVALID_REQUEST);
        }

        // 2. 가입 여부 확인 (USER_NOT_FOUND 활용)
        User user = userAuthRepository.findByEmail(userLoginRequestDto.getEmail())
                .orElseThrow(() -> new AuthException(ErrorCode.USER_NOT_FOUND));

        // 3. 비밀번호 일치 확인 (LOGIN_FAILED 활용)
        if (!passwordEncoder.matches(userLoginRequestDto.getPassword(), user.getPassword())) {
            throw new AuthException(ErrorCode.LOGIN_FAILED);
        }

        // 4. 계정 상태 확인 (ACCOUNT_DISABLED 활용)
        if (user.getStatus() != Status.ACTIVE) {
            throw new AuthException(ErrorCode.ACCOUNT_DISABLED);
        }

        try {
            Map<String, Object> claims = Map.of(
                    "email", user.getEmail(),
                    "name", user.getName()
            );

            String accessToken = jwtUtil.generateAccessToken(claims);
            String refreshToken = jwtUtil.generateRefreshToken(claims);

            log.info("로그인 성공: {}", user.getEmail());

            return UserLoginResponseDto.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .email(user.getEmail())
                    .name(user.getName())
                    .build();

        } catch (Exception e) {
            log.error("JWT 토큰 생성 중 에러 발생: ", e);
            // 시스템 내부 오류성 에러는 명확한 코드를 던지거나 RuntimeException으로 유지
            throw new RuntimeException("로그인 처리 중 보안 토큰 발행에 실패했습니다.");
        }
    }
}