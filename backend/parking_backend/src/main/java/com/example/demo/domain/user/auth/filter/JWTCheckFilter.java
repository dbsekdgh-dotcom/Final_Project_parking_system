package com.example.demo.domain.user.auth.filter;

import com.example.demo.domain.user.auth.jwt.JWTUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class JWTCheckFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        log.info("JWTCheckFilter - 현재 요청 경로: {}", path);

        // OPTIONS 요청(Preflight)은 필터를 타지 않게 설정 (CORS 해결 핵심)
        if (request.getMethod().equals("OPTIONS")) {
            return true;
        }

        // 필터를 타지 말아야 할 경로들
        if (path.startsWith("/login") ||
                path.startsWith("/oauth2") ||
                path.startsWith("/oauth-redirect") ||
                path.startsWith("/api/user/auth/refresh") ||
                path.startsWith("/api/user/auth/local/signup") ||
                path.startsWith("/api/user/auth/local/login") ||
                path.startsWith("/api/test/")
        ) {
            log.info("JWTCheckFilter - 필터 제외 경로 통과: {}", path);
            return true;
        }
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        log.info("----------------- JWTCheckFilter 실행 시작 --------------------");

        String authHeader = request.getHeader("Authorization");

        // 헤더가 없거나 Bearer로 시작하지 않으면 다음 필터로 넘김 (인증 실패가 아님, 그냥 통과)
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String accessToken = authHeader.substring(7);

            Claims claims = jwtUtil.validateToken(accessToken);

            log.info("JWT 인증 성공: {}", claims);

            String email = (String) claims.get("email");
            // role이 null일 경우를 대비해 기본값 부여
            String role = claims.get("role") != null ? (String) claims.get("role") : "ROLE_USER";

            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(
                            email,
                            null,
                            List.of(new SimpleGrantedAuthority(role))
                    );
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);

            filterChain.doFilter(request, response);
        } catch (RuntimeException e) {
            log.error("JWT 검증 실패: {}", e.getMessage());
            sendErrorResponse(response, e.getMessage());
        } catch (Exception e) {
            log.error("알 수 없는 인증 에러: {}", e.getMessage());
            sendErrorResponse(response, "INVALID_TOKEN");
        }
    }

    // 에러 응답 공통 메소드
    private void sendErrorResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=utf-8");
        PrintWriter printWriter = response.getWriter();
        printWriter.println("{\"error\": \"" + message + "\"}");
        printWriter.close();
    }
}
