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
        // 문자열로 된 비밀키(secretKey)를 JWT서명(Signature)에 사용할 수 있는 SecretKey 객체로 변환하는 코드
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        if (userSecretKey == null || userSecretKey.length() < 32) {
            throw new IllegalArgumentException("User JWT Secret Key must be at least 32 characters long!");
        }
        this.userKey = Keys.hmacShaKeyFor(userSecretKey.getBytes(StandardCharsets.UTF_8));
    }

    // ==================== 관리자 JWT 메서드 (기존 유지) ====================

    // JWT의 Header,Payload, Signatuer를 만들어 하나의 문자열 토큰으로 압축(compact)하는 코드
    // [Header] -> type:JWT
    // [Payload] -> sub, claims, iat,ext
    // [Signatuer] -> SecretKey로 서명
    //1. 토큰생성
    public String generateToken(Map<String,Object> valueMap, int min){
        String jwtStr = Jwts.builder()
                .header().type("JWT").and()
                .subject(String.valueOf(valueMap.get("loginId"))) // 토큰의 주인(사용자 식별자) -> 인증정보로 사용
                .claims(valueMap)
                .issuedAt(Date.from(ZonedDateTime.now().toInstant())) // 토큰이 생성된 시간
                .expiration(Date.from(ZonedDateTime.now().plusMinutes(min).toInstant())) // 토큰 만료시간
                .signWith(key) //Header + Payload를 SecretKey로 서명
                .compact(); //JWT 문자열 생성
        return jwtStr;
    }

    //2. 토큰 검증
    public Claims validateToken(String token){
        Claims claim=null; //Map을 상속받은 인터페이스
        try {
            claim = Jwts.parser()
                    .verifyWith(key) //시큐릿키값 설정
                    .build()
                    .parseClaimsJws(token) //유효한 토큰값이 아니면 예외발생
                    .getPayload();
        }catch (MalformedJwtException e){ //토큰이 잘못된 형식일때
            throw new AdminJWTException("MalFormed");
        }catch (ExpiredJwtException e){ //유효기간이 만료되었을때
            throw new AdminJWTException("Expired");
        }catch (InvalidClaimException e){ //유효하지 않은 토큰일때
            throw new AdminJWTException("Invalid");
        }catch (JwtException e){
            throw new AdminJWTException("JWTError");
        }catch (Exception e){
            throw new AdminJWTException("Error");
        }
        return claim;
    }

    //토큰 만료 여부 상관없이 admin 아이디 추출
    public String getAdminLoginIdWithoutValidation(String token){
        try {
            //validateToken과 달리 예외가 발생해도 Claims를 반환받기 위해 try-catch 활용
            return (String) Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseClaimsJws(token)
                    .getPayload()
                    .get("loginId");
        }catch (ExpiredJwtException e){
            //만료된 경우 예외 객체 안에 담긴 Claims에서 loginId를 꺼냄
            return (String) e.getClaims().get("loginId");
        }catch (Exception e){
            return null;
        }
    }

    // ==================== 사용자 JWT 메서드 (JWTUtil에서 통합) ====================

    // 사용자 토큰 생성 (subject: email 기반)
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

    // 사용자 AccessToken 생성 (30분)
    public String generateUserAccessToken(Map<String, Object> valueMap) {
        return generateUserToken(valueMap, 30);
    }

    // 사용자 RefreshToken 생성 (6시간)
    public String generateUserRefreshToken(Map<String, Object> valueMap) {
        return generateUserToken(valueMap, 60 * 6);
    }

    // 사용자 토큰 검증 (userKey 사용, RuntimeException 반환)
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

    // 사용자 토큰에서 인증 정보 추출 → PrincipalDetails 기반 Authentication 반환
    public Authentication getUserAuthentication(String token) {
        Claims claims = validateUserToken(token);
        String email = claims.getSubject();
        String role = (String) claims.get("role");

        if (role == null) role = "ROLE_USER";
        List<SimpleGrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority(role));

        // 메모리 상의 임시 객체 (nullable=false 필드 대응)
        User userEntity = User.builder()
                .email(email)
                .name("TEMP_USER")
                .phone("000-0000-0000")
                .birth(LocalDate.now())
                .build();

        PrincipalDetails principalDetails = new PrincipalDetails(userEntity);
        return new UsernamePasswordAuthenticationToken(principalDetails, token, authorities);
    }
}
