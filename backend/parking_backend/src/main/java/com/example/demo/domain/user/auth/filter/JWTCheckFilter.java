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
        log.info("check path: "+path);
        if (path.startsWith("/login") ||
                path.startsWith("/oauth2") ||
                path.startsWith("/oauth-redirect") ||
                path.startsWith("/api/user/auth/refresh") ||
                path.startsWith("/api/user/auth/local/signup") ||
                path.startsWith("/api/user/auth/local/login") ||
                path.startsWith("/api/test/")

        ) {
            return true;
        }
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        log.info("-----------------JWTCheckFilter 실행--------------------");

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String accessToken = authHeader.substring(7);

            Claims claims = jwtUtil.validateToken(accessToken);

            log.info("JWT 인증 성공: {}", claims);

            String email = (String) claims.get("email");
            String role = (String) claims.get("role");

            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(
                            email,
                            null,
                            List.of(new SimpleGrantedAuthority(role))
                    );
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);

            filterChain.doFilter(request, response);
        } catch (RuntimeException e) {
            log.error("JWT 검증 실패: {}",e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=utf-8");

            PrintWriter printWriter = response.getWriter();

            printWriter.println("{\"error\": \""+e.getMessage()+"\"}");
            printWriter.close();


        } catch (Exception e) {
            log.error("알 수 없는 인증 에러: {}",e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=utf-8");
            response.getWriter().println("{\"error\": \"INVALID_TOKEN\"}");
        }

    }
}
