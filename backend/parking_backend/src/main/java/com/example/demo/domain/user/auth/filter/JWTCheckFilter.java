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

        // ⭐ 원래 쓰시던 제외 경로들입니다.
        // 단, 계정 연동 시 /oauth2 경로에서도 쿠키를 읽어야 하므로
        // 쿠키가 존재할 때는 필터를 타도록 조건을 걸어주는게 안전합니다.
        if (path.startsWith("/login") ||
                path.startsWith("/oauth-redirect") ||
                path.startsWith("/api/user/auth/refresh") ||
                path.startsWith("/api/user/auth/local/signup") ||
                path.startsWith("/api/user/auth/local/login") ||
                path.startsWith("/api/test/")
        ) {
            return true;
        }

        // 소셜 로그인 시작 경로는 보통 제외하지만,
        // '연동'을 위해 쿠키를 들고 가는 경우 필터를 거쳐야 합니다.
        if (path.startsWith("/oauth2")) {
            // 쿠키 중에 temp_jwt가 있다면 필터를 수행(false 반환), 없으면 통과(true 반환)
            boolean hasTempCookie = request.getCookies() != null &&
                    Arrays.stream(request.getCookies()).anyMatch(c -> "temp_jwt".equals(c.getName()));
            return !hasTempCookie;
        }

        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        log.info("----------------- JWTCheckFilter 실행 시작 --------------------");

        String authHeader = request.getHeader("Authorization");
        String token = null;

        // 1. 헤더에서 먼저 찾기 (기존 방식)
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        }
        // 2. 헤더에 없으면 쿠키에서 찾기 (연동 방식 추가)
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

        // 토큰이 아예 없으면 그냥 다음 필터로 보냄
        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // ⭐ JWTUtil에서 이미 PrincipalDetails를 만들도록 수정했으므로,
            // 필터 코드는 이렇게 한 줄로 아주 깔끔해집니다! (중복 로직 제거)
            Authentication authentication = jwtUtil.getAuthentication(token);

            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.info("### [JWTCheckFilter] 인증 완료: {}", authentication.getName());

            filterChain.doFilter(request, response);
        } catch (RuntimeException e) {
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