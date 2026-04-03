package com.example.demo.domain.user.auth.filter;

import com.example.demo.domain.shared.user.User;
import com.example.demo.domain.user.auth.jwt.JWTUtil;
import com.example.demo.domain.user.auth.principal.PrincipalDetails;
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

        if (request.getMethod().equals("OPTIONS")) {
            return true;
        }

        // ⭐ 원래 쓰시던 경로들 100% 그대로 유지합니다!
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

        // 주의: /api/user/auth/local/link-password 는 여기 명단에 없어야 합니다.
        // 그래야 토큰 검사를 거치고 PrincipalDetails가 만들어지니까요!
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        log.info("----------------- JWTCheckFilter 실행 시작 --------------------");

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
            String role = claims.get("role") != null ? (String) claims.get("role") : "ROLE_USER";

            // ---------------------------------------------------------
            // ⭐ 핵심: 컨트롤러가 인식할 수 있게 PrincipalDetails로 포장하기
            // ---------------------------------------------------------
            User user = User.builder()
                    .email(email)
                    .build();

            PrincipalDetails principalDetails = new PrincipalDetails(user);

            // 신분증(authenticationToken)의 주인을 email(문자열)이 아닌 principalDetails(객체)로 설정!
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(
                            principalDetails,
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

    private void sendErrorResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=utf-8");
        PrintWriter printWriter = response.getWriter();
        printWriter.println("{\"error\": \"" + message + "\"}");
        printWriter.close();
    }
}