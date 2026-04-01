package com.example.demo.global.util.admin;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Map;

@Component
@Log4j2
public class AdminJWTUtil {
    private final SecretKey key;

    public AdminJWTUtil(@Value("${jwt.admin.secret}")String secretKey){
        // 문자열로 된 비밀키(secretKey)를 JWT서명(Signature)에 사용할 수 있는 SecretKey 객체로 변환하는 코드
        this.key= Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

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
    public Claims vaildateToken(String token){
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
}
