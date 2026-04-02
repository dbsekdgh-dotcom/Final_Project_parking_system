package com.example.demo.domain.user.auth.service;


import com.example.demo.domain.shared.user.User;
import com.example.demo.domain.user.auth.dtos.request.UserLoginRequestDto;
import com.example.demo.domain.user.auth.dtos.response.UserLoginResponseDto;
import com.example.demo.domain.user.auth.jwt.JWTUtil;
import com.example.demo.domain.user.auth.repository.UserAuthRepository;
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

        if (userLoginRequestDto.getEmail() == null || userLoginRequestDto.getEmail().isBlank()) {
            throw new IllegalArgumentException("이메일을 입력해 주세요.");
        }
        if (userLoginRequestDto.getPassword() == null || userLoginRequestDto.getPassword().isBlank()) {
            throw new IllegalArgumentException("비밀번호를 입력해 주세요.");
        }


        User user = userAuthRepository.findByEmail(userLoginRequestDto.getEmail())
                .orElseThrow(()-> new IllegalArgumentException("가입되지 않은 이메일 주소입니다."));

        if(!passwordEncoder.matches(userLoginRequestDto.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
        if (user.getStatus() != com.example.demo.domain.shared.user.enums.Status.ACTIVE) {
            throw new IllegalStateException("현재 사용할 수 없는 계정입니다. 관리자에게 문의하세요.");
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
            throw new RuntimeException("로그인 처리 중 보안 토큰 발행에 실패했습니다.");
        }


    }

}
