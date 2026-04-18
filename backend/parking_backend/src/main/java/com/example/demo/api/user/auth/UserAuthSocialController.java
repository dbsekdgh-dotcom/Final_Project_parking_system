package com.example.demo.api.user.auth;

import com.example.demo.domain.resident.User;
import com.example.demo.domain.auth.user.dtos.request.UserSocialRecoverRequestDto;
import com.example.demo.domain.auth.user.service.UserSocialRecoverService;
import com.example.demo.domain.auth.user.service.UserVerificationService;
import com.example.demo.global.util.admin.AdminJWTUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.HashMap;
import java.util.Map;

@Tag(name = "2. 소셜 계정 관리 (Social Auth)", description = "JWT 토큰 재발급 및 소셜 계정 복구 API")
@RestController
@RequestMapping("/api/user/auth")
@RequiredArgsConstructor
@Slf4j
public class UserAuthSocialController {

    private final AdminJWTUtil adminJWTUtil;
    private final UserSocialRecoverService userSocialRecoverService;
    private final UserVerificationService userVerificationService;

    /**
     * 토큰 재발급
     * - 기존: Authorization 헤더에서 리프레시 토큰 읽고 바디로 새 토큰 반환
     * - 변경: refreshToken 쿠키에서 읽고 새 토큰을 HttpOnly 쿠키로 설정
     */
    @Operation(summary = "JWT 토큰 재발급", description = "refreshToken 쿠키를 이용해 새로운 accessToken/refreshToken을 HttpOnly 쿠키로 재발급합니다. (토큰 불필요)")
    @PostMapping("/refresh")
    public ResponseEntity<Map<String, String>> refresh(
            @CookieValue(name = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response
    ) {
        log.info("[API] 토큰 재발급 요청");

        if (refreshToken == null || refreshToken.isBlank()) {
            return ResponseEntity.status(401).body(Map.of("error", "NO_REFRESH_TOKEN"));
        }

        try {
            // 1. JWT 기본 검증
            Claims claims = adminJWTUtil.validateUserToken(refreshToken);
            String email = claims.getSubject();
            String role = (String) claims.get("role");

            // 2. Redis 검증 (RTR 보안)
            String savedToken = userVerificationService.getRefreshToken(email);
            if (savedToken == null || !savedToken.equals(refreshToken)) {
                log.warn("토큰 불일치! 재사용 시도 혹은 탈취 가능성: {}", email);
                return ResponseEntity.status(401).body(Map.of("error", "INVALID_REFRESH_TOKEN"));
            }

            // 3. 새로운 토큰 쌍 생성
            Object userIdObj = claims.get("userId");
            Long userId = (userIdObj instanceof Number) ? ((Number) userIdObj).longValue() : null;

            Map<String, Object> newClaims = new HashMap<>();
            newClaims.put("email", email);
            newClaims.put("role", role);
            if (userId != null) newClaims.put("userId", userId);

            String newAccessToken = adminJWTUtil.generateUserAccessToken(newClaims);
            String newRefreshToken = adminJWTUtil.generateUserRefreshToken(newClaims);

            // 4. Redis 업데이트
            userVerificationService.saveRefreshToken(email, newRefreshToken);

            // 5. 새 토큰을 HttpOnly 쿠키로 설정 (세션 쿠키 - 브라우저 종료 시 삭제)
            ResponseCookie accessCookie = ResponseCookie.from("accessToken", newAccessToken)
                    .path("/").httpOnly(true).secure(false).sameSite("Lax").build();
            ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", newRefreshToken)
                    .path("/").httpOnly(true).secure(false).sameSite("Lax").build();

            response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
            response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

            log.info("토큰 재발급 및 쿠키 갱신 완료: {}", email);
            return ResponseEntity.ok(Map.of("message", "토큰이 갱신되었습니다."));

        } catch (Exception e) {
            log.error("리프레시 토큰 처리 실패: {}", e.getMessage());
            return ResponseEntity.status(401).body(Map.of("error", "REFRESH_TOKEN_EXPIRED"));
        }
    }

    /**
     * 소셜 계정 복구 후 로그인
     * - 기존: 토큰을 응답 바디에 포함
     * - 변경: 토큰을 HttpOnly 쿠키로 설정, 바디에서 제거
     */
    @Operation(summary = "소셜 계정 복구 로그인", description = "탈퇴한 소셜 계정을 복구하고 즉시 로그인 처리합니다. 토큰은 HttpOnly 쿠키로 발급됩니다. (토큰 불필요)")
    @PostMapping("/social-recover")
    public ResponseEntity<?> socialRecover(
            @Valid @RequestBody UserSocialRecoverRequestDto dto,
            HttpServletResponse response) {

        User user = userSocialRecoverService.socialRecover(dto);

        Map<String, Object> claims = Map.of(
                "email", user.getEmail(),
                "role", "USER",
                "userId", user.getUserId()
        );
        String accessToken = adminJWTUtil.generateUserAccessToken(claims);
        String refreshToken = adminJWTUtil.generateUserRefreshToken(claims);

        userVerificationService.saveRefreshToken(user.getEmail(), refreshToken);

        // maxAge 미설정 → 세션 쿠키 (브라우저 종료 시 자동 삭제)
        ResponseCookie accessCookie = ResponseCookie.from("accessToken", accessToken)
                .path("/").httpOnly(true).secure(false).sameSite("Lax").build();
        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", refreshToken)
                .path("/").httpOnly(true).secure(false).sameSite("Lax").build();

        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        return ResponseEntity.ok(Map.of(
                "message", "계정이 복구되어 로그인되었습니다.",
                "email", user.getEmail(),
                "name", user.getName()
        ));
    }
}
