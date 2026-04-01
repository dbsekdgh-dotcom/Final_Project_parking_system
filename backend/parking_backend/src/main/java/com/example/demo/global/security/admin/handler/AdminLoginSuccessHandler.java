package com.example.demo.global.security.admin.handler;

import com.example.demo.domain.admin.auth.dtos.response.AdminLoginResponse;
import com.example.demo.global.security.admin.AdminAuthDto;
import com.example.demo.global.util.admin.AdminJWTUtil;
import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Log4j2
public class AdminLoginSuccessHandler implements AuthenticationSuccessHandler {
    private final AdminJWTUtil adminJWTUtil;
    private final Gson gson;

    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication)throws IOException, ServletException {
        log.info("----------- [Admin Login Success Handler] -----------");

        //1. 인증 객체 꺼내기
        AdminAuthDto adminAuthDto = (AdminAuthDto) authentication.getPrincipal();
        Map<String,Object> claims = adminAuthDto.getClaims();
        //2. 토큰 생성 (분단위 설정)
        String accessToken=adminJWTUtil.generateToken(claims,5); //30분
        String refreshToken=adminJWTUtil.generateToken(claims,60*24); //24시간
        //3. responseDto에 담기
        AdminLoginResponse loginResponse = AdminLoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .loginId(adminAuthDto.getUsername()) //loginId
                .adminName((String)claims.get("name"))
                .build();
        //4. JSON 변환 및 응답 전송
        response.setContentType("application/json; charset=UTF-8");
        PrintWriter pw = response.getWriter();
        //DTO 객체를 JSON 문자열로 변환
        String jsonStr=gson.toJson(loginResponse);

        pw.println(jsonStr);
        pw.close();
    }
}
