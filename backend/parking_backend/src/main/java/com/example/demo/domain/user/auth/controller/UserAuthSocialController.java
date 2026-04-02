package com.example.demo.domain.user.auth.controller;


import com.example.demo.domain.user.auth.jwt.JWTUtil;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/user/auth")
@RequiredArgsConstructor
@Slf4j
public class UserAuthSocialController {

    private final JWTUtil jwtUtil;

    @PostMapping("/refresh")
    public ResponseEntity<Map<String, String>> refresh(
            @RequestHeader("Authorization") String authHeader
    ) {
        log.info("[API] 토큰 재발급 요청이 들어왔습니다.");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.error("잘못된 헤더 형식입니다.");
            return ResponseEntity.badRequest().build();
        }

        String refreshToken = authHeader.substring(7);

        try {
            Claims claims = jwtUtil.validateToken(refreshToken);

            String email = claims.getSubject();

            String role = (String) claims.get("role");

            log.info("재발급 대상 유저 이메일 : {}",email);
            log.info("재발급 대상 유저 권한: {}",role);

            Map<String, Object> newClaims = Map.of(
                    "email", email,
                    "role", role
            );

            String newAccessToken = jwtUtil.generateAccessToken(newClaims);

            String newRefreshToken = jwtUtil.generateRefreshToken(newClaims);

            log.info("새로운 토큰 발급 완료");

            return ResponseEntity.ok(Map.of(
                    "accessToken", newAccessToken,
                    "refreshToken", newRefreshToken
            ));
        }catch (Exception e) {
            log.error("리프레시 토큰이 만료되었거나 변조되었습니다 : {}",e.getMessage());
            return ResponseEntity.status(401).body(Map.of("error","REFRESH_TOKEN_EXPIRED"));
        }
    }
}
