package com.example.demo.domain.user.auth.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Map;

@Slf4j
@Component
public class JWTUtil {

    private final SecretKey key;

    public JWTUtil(@Value("${jwt.user.secret}") String secretKey) {
        // 주의: secretKey 문자열은 반드시 32자 이상이어야 합니다.
        if (secretKey == null || secretKey.length() < 32) {
            throw new IllegalArgumentException("JWT Secret Key must be at least 32 characters long!");
        }
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(Map<String, Object> valueMap, int min) {
        return Jwts.builder()
                .header().add("typ", "JWT").and() // 최신 문법: header() 사용
                .subject(String.valueOf(valueMap.get("email")))
                .claims(valueMap) // setClaims 대신 claims
                .issuedAt(Date.from(ZonedDateTime.now().toInstant()))
                .expiration(Date.from(ZonedDateTime.now().plusMinutes(min).toInstant()))
                .signWith(key) // 알고리즘(HS256)은 key를 통해 자동 인식됩니다.
                .compact();
    }

    public String generateAccessToken(Map<String, Object> valueMap) {
        return generateToken(valueMap, 60);
    }

    public String generateRefreshToken(Map<String, Object> valueMap) {
        return generateToken(valueMap, 60 *12);
    }



    public Claims validateToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(key) // 최신 문법
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (io.jsonwebtoken.security.SignatureException e) {
            // 1. 서명 오류: 누군가 토큰의 글자를 하나라도 바꿨을 때 발생
            log.error("잘못된 JWT 서명입니다: {}",e.getMessage());
            throw new RuntimeException(("INVALID_SIGNATURE"));
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            // 2. 만료 오류: 설정한 시간(1시간/12시간)이 지났을 때 발생
            log.error("만료된 JWT 토큰입니다: {}", e.getMessage());
            throw new RuntimeException("EXPIRED_TOKEN");
        } catch (io.jsonwebtoken.MalformedJwtException e) {
            // 3. 구조 오류: 토큰 형식이 아예 아닐 때 (예: "abc.123")
            log.error("유효하지 않은 구성의 JWT 토큰입니다 : {}",e.getMessage());
            throw new RuntimeException("MALFORMED_TOKEN");
        } catch (Exception e) {
            // 4. 그 외 기타 오류
            log.error("JWT 검증 중 알 수 없는 오류가 발생했습니다: {}",e.getMessage());
            throw new RuntimeException("INVALID_TOKEN");
        }
    }
}