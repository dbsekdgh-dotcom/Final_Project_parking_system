package com.example.demo.global.security.admin.handler;

import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

@Component
@Log4j2
public class AdminLoginFailureHandler implements AuthenticationFailureHandler {
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception)throws IOException, ServletException{
        log.info("----------- [Admin Login Failure Handler] -----------");
        log.error("실패 원인: "+exception.getMessage());

        //1. 응답설정
        response.setContentType("application/json; charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401

        //2. 리액트에 전달할 에러 메시지 구성
        Gson gson = new Gson();
        Map<String, Object>  errorData = Map.of(
                "error","LOGIN_FAIL",
                "message","아이디 또는 비밀번호를 확인해주세요."
        );

        //3. JSON 문자열로 변환하여 전송
        PrintWriter pw = response.getWriter();
        pw.println(gson.toJson(errorData));
        pw.close();
    }
}
