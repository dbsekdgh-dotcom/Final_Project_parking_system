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
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JWTUtil jwtUtil;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        Map<String, Object> attributes = oAuth2User.getAttributes();
        String email = null;

        log.info("### 전체 Attributes 데이터: {}", attributes);

        // 1. 네이버일 경우 (데이터가 'response' 맵 안에 있음)
        if (attributes.get("response") != null) {
            Map<String, Object> naverResponse = (Map<String, Object>) attributes.get("response");
            email = (String) naverResponse.get("email");
        }
        // 2. 카카오일 경우 (데이터가 'kakao_account' 맵 안에 있음)
        else if (attributes.get("kakao_account") != null) {
            Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
            email = (String) kakaoAccount.get("email");
        }
        // 3. 그 외 (구글 등 기본 구조)
        else {
            email = (String) attributes.get("email");
        }

        if (email == null) {
            log.error("### 이메일 추출 실패! 인증 정보를 확인하세요.");
            // 에러 페이지로 리다이렉트 하거나 예외 처리
            response.sendRedirect("http://localhost:5173/login?error=email_not_found");
            return;
        }

        log.info("소셜 로그인 성공! 추출된 이메일: {}", email);

        // JWT 생성 및 리다이렉트 로직 (기존과 동일)
        Map<String, Object> claims = Map.of("email", email, "role", "ROLE_USER");
        String accessToken = jwtUtil.generateAccessToken(claims);
        String refreshToken = jwtUtil.generateRefreshToken(claims);

        String targetUrl = UriComponentsBuilder.fromUriString("http://localhost:5173/oauth-redirect")
                .queryParam("accessToken", accessToken)
                .queryParam("refreshToken", refreshToken)
                .build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
