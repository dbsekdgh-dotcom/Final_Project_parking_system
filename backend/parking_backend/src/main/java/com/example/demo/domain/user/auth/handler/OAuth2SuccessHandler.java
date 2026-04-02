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
        // 네이버는 response 맵 안에, 카카오는 kakao_account 맵 안에 이메일이 있습니다.
        if (attributes.get("response") != null) {
            email = (String) ((Map<String, Object>) attributes.get("response")).get("email");
        } else if (attributes.get("kakao_account") != null) {
            email = (String) ((Map<String, Object>) attributes.get("kakao_account")).get("email");
        } else {
            email = (String) attributes.get("email");
        }

        log.info("소셜 로그인 성공 이메일: {}", email);

        Map<String, Object> claims = Map.of(
                "email", email,
                "role", "ROLE_USER"
        );

        // [중요] 아까 만든 generateAccessToken, generateRefreshToken 메서드명을 사용하세요!
        String accessToken = jwtUtil.generateAccessToken(claims);
        String refreshToken = jwtUtil.generateRefreshToken(claims);

        String targetUrl = UriComponentsBuilder.fromUriString("http://localhost:3000/oauth-redirect")
                .queryParam("accessToken", accessToken)
                .queryParam("refreshToken", refreshToken)
                .build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
