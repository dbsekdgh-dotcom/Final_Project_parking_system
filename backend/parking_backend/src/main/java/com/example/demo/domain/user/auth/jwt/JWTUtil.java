package com.example.demo.domain.user.auth.jwt;

import com.example.demo.domain.shared.user.User;
import com.example.demo.domain.user.auth.principal.PrincipalDetails;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Component
public class JWTUtil {

    private final SecretKey key;

    public JWTUtil(@Value("${jwt.user.secret}") String secretKey) {
        if (secretKey == null || secretKey.length() < 32) {
            throw new IllegalArgumentException("JWT Secret Key must be at least 32 characters long!");
        }
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 토큰에서 인증 정보를 추출하여 PrincipalDetails 객체를 담은 Authentication 반환
     */
    public Authentication getAuthentication(String token) {
        Claims claims = validateToken(token);
        String email = claims.getSubject();
        String role = (String) claims.get("role");

        if (role == null) role = "ROLE_USER";
        List<SimpleGrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority(role));

        // 1. User 엔티티 생성 (엔티티의 @Builder 필수값 대응)
        // 메모리 상의 임시 객체이므로 이메일을 제외한 값은 임의로 채웁니다.
        User userEntity = User.builder()
                .email(email)
                .name("TEMP_USER")        // nullable = false 대응
                .phone("000-0000-0000")   // nullable = false 대응
                .birth(LocalDate.now())    // nullable = false 대응
                .build();

        // 2. PrincipalDetails 생성
        PrincipalDetails principalDetails = new PrincipalDetails(userEntity);

        // 3. 스프링 시큐리티 인증 객체 생성 및 반환
        return new UsernamePasswordAuthenticationToken(principalDetails, token, authorities);
    }

    public String generateToken(Map<String, Object> valueMap, int min) {
        return Jwts.builder()
                .header().add("typ", "JWT").and()
                .subject(String.valueOf(valueMap.get("email")))
                .claims(valueMap)
                .issuedAt(Date.from(ZonedDateTime.now().toInstant()))
                .expiration(Date.from(ZonedDateTime.now().plusMinutes(min).toInstant()))
                .signWith(key)
                .compact();
    }

    public String generateAccessToken(Map<String, Object> valueMap) {
        return generateToken(valueMap, 60);
    }

    public String generateRefreshToken(Map<String, Object> valueMap) {
        return generateToken(valueMap, 60 * 12);
    }

    public Claims validateToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (io.jsonwebtoken.security.SignatureException e) {
            log.error("잘못된 JWT 서명입니다: {}", e.getMessage());
            throw new RuntimeException("INVALID_SIGNATURE");
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            log.error("만료된 JWT 토큰입니다: {}", e.getMessage());
            throw new RuntimeException("EXPIRED_TOKEN");
        } catch (io.jsonwebtoken.MalformedJwtException e) {
            log.error("유효하지 않은 구성의 JWT 토큰입니다 : {}", e.getMessage());
            throw new RuntimeException("MALFORMED_TOKEN");
        } catch (Exception e) {
            log.error("JWT 검증 중 알 수 없는 오류가 발생했습니다: {}", e.getMessage());
            throw new RuntimeException("INVALID_TOKEN");
        }
    }
}