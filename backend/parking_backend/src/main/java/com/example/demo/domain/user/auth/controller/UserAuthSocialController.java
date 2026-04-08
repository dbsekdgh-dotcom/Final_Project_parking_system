package com.example.demo.domain.user.auth.controller;


import com.example.demo.domain.shared.user.User;
import com.example.demo.domain.user.auth.dtos.request.UserSocialRecoverRequestDto;
import com.example.demo.domain.user.auth.service.UserSocialRecoverService;
import com.example.demo.global.util.admin.AdminJWTUtil;
import io.jsonwebtoken.Claims;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user/auth")
@RequiredArgsConstructor
@Slf4j
public class UserAuthSocialController {

    private final AdminJWTUtil adminJWTUtil;
    private final UserSocialRecoverService userSocialRecoverService;

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
            Claims claims = adminJWTUtil.validateUserToken(refreshToken);

            String email = claims.getSubject();

            String role = (String) claims.get("role");

            log.info("재발급 대상 유저 이메일 : {}",email);
            log.info("재발급 대상 유저 권한: {}",role);

            Map<String, Object> newClaims = Map.of(
                    "email", email,
                    "role", role
            );

            String newAccessToken = adminJWTUtil.generateUserAccessToken(newClaims);

            String newRefreshToken = adminJWTUtil.generateUserRefreshToken(newClaims);

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
    @PostMapping("/social-recover")
    public ResponseEntity<?> socialRecover(
            @Valid @RequestBody UserSocialRecoverRequestDto userSocialRecoverRequestDto) {

        log.info("[API] 소셜 계정 복구 요청 - Email: {}", userSocialRecoverRequestDto.getEmail());

        User user = userSocialRecoverService.socialRecover(userSocialRecoverRequestDto);

        Map<String, Object> claims = Map.of(
                "email", user.getEmail(),
                "role","USER"
        );

        String accessToken = adminJWTUtil.generateUserAccessToken(claims);
        String refreshToken = adminJWTUtil.generateUserRefreshToken(claims);

        return ResponseEntity.ok(Map.of(
                "message", "계정이 성공적으로 복구되어 자동 로그인되었습니다.",
                "accessToken", accessToken,
                "refreshToken", refreshToken,
                "email", user.getEmail(),
                "name", user.getName()
        ));
    }

}
