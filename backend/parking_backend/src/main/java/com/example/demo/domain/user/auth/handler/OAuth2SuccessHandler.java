package com.example.demo.domain.user.auth.handler;

import com.example.demo.domain.user.auth.jwt.JWTUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JWTUtil jwtUtil;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        log.info("----------------- OAuth2SuccessHandler 실행 -----------------");

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        Map<String, Object> attributes = oAuth2User.getAttributes();

        String email = null;

        // [수정 포인트 1] 네이버/카카오 등 소셜 제공자별 이메일 추출 경로 대응
        if (attributes.get("response") != null) { // 네이버의 경우
            Map<String, Object> responseMap = (Map<String, Object>) attributes.get("response");
            email = (String) responseMap.get("email");
        } else if (attributes.get("kakao_account") != null) { // 카카오의 경우
            Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
            email = (String) kakaoAccount.get("email");
        } else {
            email = (String) attributes.get("email");
        }

        log.info("소셜 로그인 성공! 추출된 이메일: {}", email);

        // [수정 포인트 2] Map.of는 null을 허용하지 않으므로 HashMap을 사용하거나 null 체크 필요
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", email);
        claims.put("role", "ROLE_USER");

        // 토큰 생성 (기존 로직 유지)
        String accessToken = jwtUtil.generateToken(claims, 2);

        // [수정 포인트 3] 기존에 사용하시던 리액트 주소로 리다이렉트
        String targetUrl = UriComponentsBuilder.fromUriString("http://localhost:3000/oauth-redirect")
                .queryParam("token", accessToken)
                .build().toUriString();

        log.info("리다이렉트 타겟 URL: {}", targetUrl);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}