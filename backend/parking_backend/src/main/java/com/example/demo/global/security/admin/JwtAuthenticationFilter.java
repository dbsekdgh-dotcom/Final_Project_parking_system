package com.example.demo.global.security.admin;

import com.example.demo.global.util.admin.AdminJWTUtil;
import com.google.gson.Gson;
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
import java.util.Map;

@Log4j2
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final AdminJWTUtil adminJWTUtil;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        log.info("----------- [JWT Filter] Checking path: " + path + " -----------");

        // 관리자 인증 불필요 경로 (기존 유지)
        if (path.startsWith("/admin/login")
                || path.startsWith("/admin/refresh")
                || path.startsWith("/admin/logout")
                || path.startsWith("/mypage")) {
            return true;
        }

        // 사용자 인증 불필요 경로 (JWTCheckFilter에서 통합)
        if (request.getMethod().equals("OPTIONS")) {
            return true;
        }
        if (path.startsWith("/login")
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
                || path.startsWith("/api/test/"
        )) {
            return true;
        }

        if (path.startsWith("/oauth2")) {
            boolean hasTempCookie = request.getCookies() != null &&
                    Arrays.stream(request.getCookies()).anyMatch(c -> "temp_jwt".equals(c.getName()));
            return !hasTempCookie;
        }

        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();

        if (path.startsWith("/admin")) { // 기존 "/admin/" 에서 "/" 제거 (더 확실하게 매칭)
            handleAdminJwt(request, response, filterChain);
        } else {
            handleUserJwt(request, response, filterChain);
        }
    }

    // ==================== 관리자 JWT 처리 (기존 doFilterInternal 내용 유지) ====================

    private void handleAdminJwt(HttpServletRequest request, HttpServletResponse response,
                                FilterChain filterChain) throws ServletException, IOException {
        // 1. 헤더에서 Authorization값을 가져옴
        String headerAuth = request.getHeader("Authorization");
        log.info("----------- [Admin JWT Filter] Authorization Header: " + headerAuth + " -----------");
        // 2. 토큰이 없거나 "Bearer "로 시작하지 않으면 다음 필터로 진행(인증 미처리)
        if (headerAuth == null || !headerAuth.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        // 3. "Bearer "뒷부분의 실제 토큰 추출
        String accessToken = headerAuth.substring(7);

        try {
            // 4. 토큰 검증 및 내부 데이터(Claims) 추출
            Claims claims = adminJWTUtil.validateToken(accessToken);
            log.info("----------- [Admin JWT Filter] Token Validated. Claims: " + claims + " -----------");
            // 5. 토큰 정보를 바탕으로 AdminAuthDto 객체 생성
            String loginId = (String) claims.get("loginId");
            String name = (String) claims.get("name");
            AdminAuthDto adminAuthDto = new AdminAuthDto(loginId, "pw_hidden", name);
            // 6. 스프링 시큐리티 전용 인증 토큰 생성
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(adminAuthDto, null, adminAuthDto.getAuthorities());
            // 7. 시큐리티 메모리(Context)에 "이 사람 인증됨"이라고 기록
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            // 8. 다음 필터로 이동
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            // 토큰이 만료되었거나 변조된 경우 에러 처리
            log.error("----------- [Admin JWT Filter] Token Error: " + e.getMessage() + " -----------");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json; charset=UTF-8");
            Gson gson = new Gson();
            String jsonStr = gson.toJson(Map.of("error", "ERROR_ACCESS_TOKEN"));
            PrintWriter pw = response.getWriter();
            pw.println(jsonStr);
            pw.close();
        }
    }

    // ==================== 사용자 JWT 처리 (JWTCheckFilter에서 통합) ====================

    private void handleUserJwt(HttpServletRequest request, HttpServletResponse response,
                               FilterChain filterChain) throws ServletException, IOException {
        log.info("----------------- [User JWT Filter] 실행 시작 (Path: {}) --------------------", request.getRequestURI());

        String authHeader = request.getHeader("Authorization");
        String token = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        } else if (request.getCookies() != null) {
            token = Arrays.stream(request.getCookies())
                    .filter(cookie -> "temp_jwt".equals(cookie.getName()))
                    .map(Cookie::getValue)
                    .findFirst()
                    .orElse(null);

            if (token != null) {
                log.info("### [User JWT Filter] 쿠키에서 토큰 발견!");
            }
        }

        // 토큰이 없으면 그냥 통과 (이후 SecurityConfig의 authorizeHttpRequests에서 걸러짐)
        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            Authentication authentication = adminJWTUtil.getUserAuthentication(token);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.info("### [User JWT Filter] 인증 완료: {}", authentication.getName());
            filterChain.doFilter(request, response);
        } catch (RuntimeException e) {
            log.error("JWT 검증 실패: {}", e.getMessage());
            sendUserErrorResponse(response, e.getMessage());
        } catch (Exception e) {
            log.error("알 수 없는 인증 에러: {}", e.getMessage());
            sendUserErrorResponse(response, "INVALID_TOKEN");
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