package com.example.demo.global.util.admin;

import com.example.demo.domain.shared.user.User;
import com.example.demo.domain.user.auth.principal.PrincipalDetails;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Component
@Log4j2
public class AdminJWTUtil {

    private final SecretKey key;
    private final SecretKey userKey;

    public AdminJWTUtil(@Value("${jwt.admin.secret}") String secretKey,
                        @Value("${jwt.user.secret}") String userSecretKey) {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));

        if (userSecretKey == null || userSecretKey.length() < 32) {
            throw new IllegalArgumentException("User JWT Secret Key must be at least 32 characters long!");
        }
        this.userKey = Keys.hmacShaKeyFor(userSecretKey.getBytes(StandardCharsets.UTF_8));
    }

    // ==================== 관리자 JWT 메서드 ====================

    public String generateToken(Map<String, Object> valueMap, int min) {
        return Jwts.builder()
                .header().type("JWT").and()
                .subject(String.valueOf(valueMap.get("loginId")))
                .claims(valueMap)
                .issuedAt(Date.from(ZonedDateTime.now().toInstant()))
                .expiration(Date.from(ZonedDateTime.now().plusMinutes(min).toInstant()))
                .signWith(key)
                .compact();
    }

    public Claims validateToken(String token) {
        Claims claim = null;
        try {
            claim = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseClaimsJws(token)
                    .getPayload();
        } catch (MalformedJwtException e) {
            throw new AdminJWTException("MalFormed");
        } catch (ExpiredJwtException e) {
            throw new AdminJWTException("Expired");
        } catch (InvalidClaimException e) {
            throw new AdminJWTException("Invalid");
        } catch (JwtException e) {
            throw new AdminJWTException("JWTError");
        } catch (Exception e) {
            throw new AdminJWTException("Error");
        }
        return claim;
    }

    public String getAdminLoginIdWithoutValidation(String token) {
        try {
            return (String) Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseClaimsJws(token)
                    .getPayload()
                    .get("loginId");
        } catch (ExpiredJwtException e) {
            return (String) e.getClaims().get("loginId");
        } catch (Exception e) {
            return null;
        }
    }

    // ==================== 사용자 JWT 메서드 ====================

    public String generateUserToken(Map<String, Object> valueMap, int min) {
        return Jwts.builder()
                .header().add("typ", "JWT").and()
                .subject(String.valueOf(valueMap.get("email")))
                .claims(valueMap)
                .issuedAt(Date.from(ZonedDateTime.now().toInstant()))
                .expiration(Date.from(ZonedDateTime.now().plusMinutes(min).toInstant()))
                .signWith(userKey)
                .compact();
    }

    public String generateUserAccessToken(Map<String, Object> valueMap) {
        return generateUserToken(valueMap, 30);
    }

    public String generateUserRefreshToken(Map<String, Object> valueMap) {
        return generateUserToken(valueMap, 60 * 6);
    }

    public Claims validateUserToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(userKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (io.jsonwebtoken.security.SignatureException e) {
            log.error("잘못된 JWT 서명입니다: {}", e.getMessage());
            throw new RuntimeException("INVALID_SIGNATURE");
        } catch (ExpiredJwtException e) {
            log.error("만료된 JWT 토큰입니다: {}", e.getMessage());
            throw new RuntimeException("EXPIRED_TOKEN");
        } catch (MalformedJwtException e) {
            log.error("유효하지 않은 구성의 JWT 토큰입니다: {}", e.getMessage());
            throw new RuntimeException("MALFORMED_TOKEN");
        } catch (Exception e) {
            log.error("JWT 검증 중 알 수 없는 오류가 발생했습니다: {}", e.getMessage());
            throw new RuntimeException("INVALID_TOKEN");
        }
    }

    /**
     * 사용자 토큰에서 인증 정보 추출 (PrincipalDetails에 userId 포함)
     */
    public Authentication getUserAuthentication(String token) {
        Claims claims = validateUserToken(token);
        String email = claims.getSubject();
        String role = (String) claims.get("role");

        // ⭐ 핵심: 토큰의 Claims에서 userId를 안전하게 추출
        Object userIdObj = claims.get("userId");
        Long userId = null;
        if (userIdObj instanceof Number) {
            userId = ((Number) userIdObj).longValue();
        }

        if (role == null) {
            role = "ROLE_USER";
        }

        List<SimpleGrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority(role));

        // ⭐ 임시 엔티티를 생성하여 PrincipalDetails에 주입
        User userEntity = User.builder()
                .userId(userId)
                .email(email)
                .name("TEMP_USER")
                .phone("000-0000-0000")
                .birth(LocalDate.now())
                .build();

        PrincipalDetails principalDetails = new PrincipalDetails(userEntity);

        return new UsernamePasswordAuthenticationToken(principalDetails, token, authorities);
    }
}