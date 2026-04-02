package com.example.demo.global.security.admin.handler;

import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

@Component
@Log4j2
public class AdminAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException{
        log.info("----------- [Admin Login Denied] -----------");
        log.error("접근 거부 경로: "+request.getRequestURI());

        //1. 응답설정
        response.setStatus(HttpServletResponse.SC_FORBIDDEN); // 403
        response.setContentType("application/json; charset=UTF-8");
        //2. 메시지 구성
        Gson gson = new Gson();
        Map<String,Object> errorData=Map.of(
                "error","ERROR_ACCESS_DENIED",
                "message","해당 메뉴에 대한 접근 권한이 없습니다."
        );
        //3. 전송
        PrintWriter pw = response.getWriter();
        pw.println(errorData);
        pw.close();
    }
}
