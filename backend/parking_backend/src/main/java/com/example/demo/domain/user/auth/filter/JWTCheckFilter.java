package com.example.demo.domain.user.auth.filter;

import com.example.demo.domain.user.auth.jwt.JWTUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;

@Slf4j
@RequiredArgsConstructor
public class JWTCheckFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();

        if (request.getMethod().equals("OPTIONS")) {
            return true;
        }

        // ⭐ [수정] 아이디/비밀번호 찾기 관련 경로들을 제외 목록에 추가합니다.
        if (path.startsWith("/login") ||
                path.startsWith("/oauth-redirect") ||
                path.startsWith("/api/user/auth/refresh") ||
                path.startsWith("/api/user/auth/local/signup") ||
                path.startsWith("/api/user/auth/local/login") ||
                path.startsWith("/api/user/auth/local/find-email") ||  // 추가
                path.startsWith("/api/user/auth/local/send-code") ||   // 추가
                path.startsWith("/api/user/auth/local/verify-code") || // 추가
                path.startsWith("/api/user/auth/local/reset-password") || // 추가
                path.startsWith("/api/test/")
        ) {
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
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        log.info("----------------- JWTCheckFilter 실행 시작 (Path: {}) --------------------", request.getRequestURI());

        String authHeader = request.getHeader("Authorization");
        String token = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        }
        else if (request.getCookies() != null) {
            token = Arrays.stream(request.getCookies())
                    .filter(cookie -> "temp_jwt".equals(cookie.getName()))
                    .map(Cookie::getValue)
                    .findFirst()
                    .orElse(null);

            if (token != null) {
                log.info("### [JWTCheckFilter] 쿠키에서 토큰 발견!");
            }
        }

        // 토큰이 없으면 그냥 통과 (이후 SecurityConfig의 authorizeHttpRequests에서 걸러짐)
        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // 토큰이 있을 때만 검증 실행
            Authentication authentication = jwtUtil.getAuthentication(token);

            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.info("### [JWTCheckFilter] 인증 완료: {}", authentication.getName());

            filterChain.doFilter(request, response);
        } catch (RuntimeException e) {
            // 만약 permitAll 대상인데 만료된 토큰이 넘어온 경우를 대비해
            // 여기서 에러를 내기보다 로그만 찍고 통과시키는 방법도 있지만,
            // 보통은 위 shouldNotFilter에서 경로를 완벽히 막아주는 게 정석입니다.
            log.error("JWT 검증 실패: {}", e.getMessage());
            sendErrorResponse(response, e.getMessage());
        } catch (Exception e) {
            log.error("알 수 없는 인증 에러: {}", e.getMessage());
            sendErrorResponse(response, "INVALID_TOKEN");
        }
    }

    private void sendErrorResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=utf-8");
        PrintWriter printWriter = response.getWriter();
        printWriter.println("{\"error\": \"" + message + "\"}");
        printWriter.close();
    }
}