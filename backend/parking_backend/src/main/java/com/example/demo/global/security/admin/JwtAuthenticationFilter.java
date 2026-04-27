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
        if (path.startsWith("/api/admin/login")
                || path.startsWith("/api/admin/refresh")
                || path.startsWith("/api/admin/logout")
                || path.startsWith("/mypage")
                || path.startsWith("/admin/login")
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

        System.out.println(">>> [필터 내부] 로직 실행 시작: " + path);

        if (path.startsWith("/api/admin") || path.startsWith("/admin")) {
            handleAdminJwt(request, response, filterChain);
        } else {
            handleUserJwt(request, response, filterChain);
        }
    }

    /**
     * 사용자용 JWT 인증 처리 (HttpOnly 쿠키 기반으로 수정)
     */
    private void handleUserJwt(HttpServletRequest request, HttpServletResponse response,
                               FilterChain filterChain) throws ServletException, IOException {

        String token = null;

        // 1. [우선순위 1] 쿠키 확인 (HttpOnly 쿠키 방식)
        if (request.getCookies() != null) {
            token = Arrays.stream(request.getCookies())
                    .filter(cookie -> "accessToken".equals(cookie.getName())) // 우리가 서비스에서 정한 이름
                    .map(Cookie::getValue)
                    .findFirst()
                    .orElse(null);
        }

        // 2. [우선순위 2] 헤더 확인 (기존 방식 호환용 - 필요 없다면 삭제 가능)
        if (token == null) {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
            }
        }

        // 토큰이 아예 없는 경우
        if (token == null) {
            log.info(">>> [인증 토큰 없음] 경로: {}", request.getRequestURI());
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // 토큰 검증 및 Authentication 객체 생성
            Authentication authentication = adminJWTUtil.getUserAuthentication(token);

            // 시큐리티 컨텍스트에 인증 정보 저장 (이후 컨트롤러에서 Principal 사용 가능)
            SecurityContextHolder.getContext().setAuthentication(authentication);

            log.info(">>> [인증 성공] 유저: {}", authentication.getName());
            filterChain.doFilter(request, response);

        } catch (Exception e) {
            log.error(">>> [인증 실패] 토큰 무효: {}", e.getMessage());
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
            String loginId = (String) claims.get("loginId");
            String name = (String) claims.get("name");
            AdminAuthDto adminAuthDto = new AdminAuthDto(loginId,"pw_hidden",name);
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(adminAuthDto,null,adminAuthDto.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
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