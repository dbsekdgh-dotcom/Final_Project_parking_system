package com.example.demo.domain.admin.controller;

import com.example.demo.global.util.admin.AdminJWTUtil;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@Log4j2
public class AdminRefreshController {
    private final AdminJWTUtil adminJWTUtil;

    @RequestMapping("/admin/refresh")
    public Map<String,Object> refresh(@RequestHeader("Authorization")String authHeader,
                                      @RequestParam("refreshToken")String refreshToken){
        log.info("----------- [Admin Token Refresh] 시작 -----------");

        //1. 기본 검증(토큰이 비어있는지 등)
        if (refreshToken==null)throw new RuntimeException("NULL_REFRESH");
        if (authHeader==null || authHeader.length() < 7)throw new RuntimeException("INVALID_HEADER");

        String accessToken = authHeader.substring(7);

        //2. Access 토큰이 아직 살아있다면? 그대로 반환
        if(!isExpired(accessToken)){
            return Map.of("accessToken",accessToken,"refreshToken",refreshToken);
        }
        //3. Access토큰이 만료되었다면 Refresh토큰 검증
        Claims claims = adminJWTUtil.validateToken(refreshToken);
        //4. 새로운 Access토큰 발급(예:5분)
        String newAccessToken = adminJWTUtil.generateToken(Map.of(
                "loginId",claims.get("loginId"),
                "name",claims.get("name")
        ),5);
        //5. Refresh토큰도 만료 임박(1시간 미만)했다면 같이 갱신
        String newRefreshToken = refreshToken;
        if(checkTime((Long) claims.get("exp"))){
            newRefreshToken = adminJWTUtil.generateToken(Map.of(
                    "loginId",claims.get("loginId"),
                    "name",claims.get("name")
            ), 60 * 24); // 24시간으로 갱신
        }
        return Map.of("accessToken",newAccessToken,"refreshToken",newRefreshToken);
    }

    private boolean isExpired(String token){
        try {
            adminJWTUtil.validateToken(token);
            return false;
        }catch (Exception e){
            return true; // 예외가 발생하면 만료되었거나 문제가 있는 토큰으로 간주
        }
    }

    private boolean checkTime(Long exp){
        long expMillis = exp * 1000L; //밀리세컨즈 단위로 들어가야하기때문에 *1000
        //현재 시간과의 차이 계산 - 밀리세컨즈
        long gap = expMillis - System.currentTimeMillis();
        //분단위 계산
        long leftMin = gap / (1000 * 60);
        //1시간도 안남았는지..
        return leftMin < 60;
    }
}
