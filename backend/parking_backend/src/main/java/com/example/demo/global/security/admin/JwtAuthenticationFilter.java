package com.example.demo.global.security.admin;

import com.example.demo.global.util.admin.AdminJWTUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;

@Log4j2
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final AdminJWTUtil adminJWTUtil;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        String method = request.getMethod();

        // [로그 추가] 요청이 서버 입구에 도착했는지 확인
        System.out.println(">>> [입구 감지] Path: " + path + " | Method: " + method);

        // 1. OPTIONS 메서드는 CORS 처리를 위해 doFilterInternal에서 처리하도록 필터를 통과시킴
        if (method.equals("OPTIONS")) {
            return false;
        }

        // 2. 인증 불필요 경로 정의
        if (path.startsWith("/admin/login")
                || path.startsWith("/admin/refresh")
                || path.startsWith("/admin/logout")
                || path.startsWith("/mypage")
                || path.startsWith("/login")
                || path.startsWith("/oauth-redirect")
                || path.startsWith("/api/user/auth/refresh")
                || path.startsWith("/api/user/auth/local/signup")
                || path.startsWith("/api/user/auth/local/check-email")
                || path.startsWith("/api/user/auth/local/login")
                || path.startsWith("/api/user/auth/local/find-email")
                || path.startsWith("/api/user/auth/local/send-code")
                || path.startsWith("/api/user/auth/local/verify-code")
                || path.startsWith("/api/user/auth/local/reset-password")
                || path.startsWith("/api/user/auth/local/send-recover-code")
                || path.startsWith("/api/user/auth/local/recover")
                || path.startsWith("/api/user/auth/local/verify-recover-code")
                || path.startsWith("/api/user/auth/social-recover")
                || path.startsWith("/api/test/")) {
            return true;
        }

        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();
        String method = request.getMethod();

        // [CORS 강제 해결] 브라우저 프리플라이트 요청 응답
        if ("OPTIONS".equalsIgnoreCase(method)) {
            System.out.println(">>> [OPTIONS 응답] CORS 헤더 강제 주입 중...");
            response.setHeader("Access-Control-Allow-Origin", "http://localhost:5202");
            response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, PATCH, OPTIONS");
            response.setHeader("Access-Control-Allow-Headers", "*");
            response.setHeader("Access-Control-Allow-Credentials", "true");
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        System.out.println(">>> [필터 내부] 로직 실행 시작: " + path);

        if (path.startsWith("/admin")) {
            handleAdminJwt(request, response, filterChain);
        } else {
            handleUserJwt(request, response, filterChain);
        }
    }

    private void handleUserJwt(HttpServletRequest request, HttpServletResponse response,
                               FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        System.out.println(">>> [헤더 확인] Authorization: " + authHeader);

        String token = null;

        // 1. Authorization 헤더 확인
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        }
        // 2. 쿠키 확인 (헤더에 없을 경우)
        else if (request.getCookies() != null) {
            token = Arrays.stream(request.getCookies())
                    .filter(cookie -> "temp_jwt".equals(cookie.getName()))
                    .map(Cookie::getValue)
                    .findFirst()
                    .orElse(null);
        }

        if (token == null) {
            System.out.println(">>> [토큰 없음] 필터 통과 (SecurityConfig에서 차단될 예정)");
            filterChain.doFilter(request, response);
            return;
        }

        try {
            System.out.println(">>> [토큰 검증] 토큰 존재, 유저 정보 추출 중...");
            Authentication authentication = adminJWTUtil.getUserAuthentication(token);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            System.out.println(">>> [인증 성공] Principal: " + authentication.getName());
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            System.out.println(">>> [인증 실패] 원인: " + e.getMessage());
            sendUserErrorResponse(response, "INVALID_TOKEN");
        }
    }

    private void handleAdminJwt(HttpServletRequest request, HttpServletResponse response,
                                FilterChain filterChain) throws ServletException, IOException {
        String headerAuth = request.getHeader("Authorization");

        if (headerAuth == null || !headerAuth.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String accessToken = headerAuth.substring(7);
        try {
            Claims claims = adminJWTUtil.validateToken(accessToken);
            // 관리자 인증 관련 추가 로직이 필요한 경우 여기에 구현
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            sendUserErrorResponse(response, "ERROR_ACCESS_TOKEN");
        }
    }

    private void sendUserErrorResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=utf-8");

        PrintWriter printWriter = response.getWriter();
        printWriter.println("{\"error\": \"" + message + "\"}");
        printWriter.close();
    }
}