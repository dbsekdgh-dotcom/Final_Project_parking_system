package com.example.demo.domain.user.auth.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Map;

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

    public Claims validateToken(String token) {
        return Jwts.parser()
                .verifyWith(key) // 최신 문법
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}