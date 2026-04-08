package com.example.demo.domain.user.auth.controller;

import com.example.demo.domain.shared.user.User;
import com.example.demo.domain.user.auth.dtos.request.UserSocialRecoverRequestDto;
import com.example.demo.domain.user.auth.service.UserSocialRecoverService;
import com.example.demo.domain.user.auth.service.UserVerificationService;
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
    private final UserVerificationService userVerificationService; // ⭐ 주입 추가

    @PostMapping("/refresh")
    public ResponseEntity<Map<String, String>> refresh(
            @RequestHeader("Authorization") String authHeader
    ) {
        log.info("[API] 토큰 재발급 요청");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().build();
        }

        String refreshToken = authHeader.substring(7);

        try {
            // 1. JWT 기본 검증 (변조/시간 만료 여부)
            Claims claims = adminJWTUtil.validateUserToken(refreshToken);
            String email = claims.getSubject();
            String role = (String) claims.get("role");

            // 2. ⭐ Redis 검증 (RTR 보안: 서버에 저장된 현재 토큰과 일치하는지)
            String savedToken = userVerificationService.getRefreshToken(email);
            if (savedToken == null || !savedToken.equals(refreshToken)) {
                log.warn("토큰 불일치! 재사용 시도 혹은 탈취 가능성: {}", email);
                return ResponseEntity.status(401).body(Map.of("error", "INVALID_REFRESH_TOKEN"));
            }

            // 3. 새로운 토큰 쌍 생성
            Map<String, Object> newClaims = Map.of("email", email, "role", role);
            String newAccessToken = adminJWTUtil.generateUserAccessToken(newClaims);
            String newRefreshToken = adminJWTUtil.generateUserRefreshToken(newClaims);

            // 4. ⭐ Redis 업데이트 (기존 토큰 무효화 및 새 토큰 저장)
            userVerificationService.saveRefreshToken(email, newRefreshToken);

            log.info("새로운 토큰 쌍 발급 및 Redis 갱신 완료: {}", email);

            return ResponseEntity.ok(Map.of(
                    "accessToken", newAccessToken,
                    "refreshToken", newRefreshToken
            ));
        } catch (Exception e) {
            log.error("리프레시 토큰 처리 실패: {}", e.getMessage());
            return ResponseEntity.status(401).body(Map.of("error", "REFRESH_TOKEN_EXPIRED"));
        }
    }

    @PostMapping("/social-recover")
    public ResponseEntity<?> socialRecover(@Valid @RequestBody UserSocialRecoverRequestDto dto) {
        User user = userSocialRecoverService.socialRecover(dto);

        Map<String, Object> claims = Map.of("email", user.getEmail(), "role", "USER");
        String accessToken = adminJWTUtil.generateUserAccessToken(claims);
        String refreshToken = adminJWTUtil.generateUserRefreshToken(claims);

        // ⭐ 로그인 성공 시 Redis에 리프레시 토큰 저장
        userVerificationService.saveRefreshToken(user.getEmail(), refreshToken);

        return ResponseEntity.ok(Map.of(
                "message", "계정이 복구되어 로그인되었습니다.",
                "accessToken", accessToken,
                "refreshToken", refreshToken,
                "email", user.getEmail(),
                "name", user.getName()
        ));
    }
}