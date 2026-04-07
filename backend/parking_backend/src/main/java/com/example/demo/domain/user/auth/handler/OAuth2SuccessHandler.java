package com.example.demo.domain.user.auth.handler;

import com.example.demo.domain.shared.user.User;
import com.example.demo.domain.shared.user.enums.Status;
import com.example.demo.global.util.admin.AdminJWTUtil;
import com.example.demo.domain.user.auth.repository.UserAuthRepository;
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
import java.time.LocalDate; // ⭐ 추가
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final AdminJWTUtil adminJWTUtil;
    private final UserAuthRepository userAuthRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        Map<String, Object> attributes = oAuth2User.getAttributes();
        String email = null;
        String name = "Social User";
        LocalDate birthDate = LocalDate.of(1900, 1, 1); // 기본값 설정 (엔티티가 nullable=false 이므로)
        String phone = "010-0000-0000"; // 기본값 설정 (엔티티가 nullable=false 이므로)

        log.info("### 소셜 로그인 시도 - 전체 Attributes: {}", attributes);

        // 1. 서비스별 정보 추출
        if (attributes.get("response") != null) { // 네이버
            Map<String, Object> naverResponse = (Map<String, Object>) attributes.get("response");
            email = (String) naverResponse.get("email");
            name = (String) naverResponse.get("name");

            // 전화번호 추출 및 포맷팅 (010-1234-5678 형태라면 그대로 사용)
            String mobile = (String) naverResponse.get("mobile");
            if (mobile != null) phone = mobile;

            // 생년월일 조합 및 LocalDate 변환
            String year = (String) naverResponse.get("birthyear");
            String day = (String) naverResponse.get("birthday");
            if (year != null && day != null) {
                try {
                    birthDate = LocalDate.parse(year + "-" + day); // "1994-09-07" -> LocalDate
                } catch (Exception e) {
                    log.warn("### 생년월일 파싱 실패, 기본값 사용");
                }
            }
        } else if (attributes.get("kakao_account") != null) { // 카카오
            Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
            email = (String) kakaoAccount.get("email");
            Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
            if (profile != null) name = (String) profile.get("nickname");
        } else {
            email = (String) attributes.get("email");
            name = (String) attributes.get("name");
        }

        if (email == null) {
            log.error("### 이메일 추출 실패!");
            response.sendRedirect("http://localhost:5173/login?error=email_not_found");
            return;
        }

        final String finalEmail = email;
        final String finalName = (name != null) ? name : "소셜사용자";
        final LocalDate finalBirth = birthDate;
        final String finalPhone = phone;

        // 2. DB 저장 로직
        try {
            userAuthRepository.findByEmail(finalEmail).orElseGet(() -> {
                log.info("### 신규 소셜 사용자 등록: {}", finalEmail);
                return userAuthRepository.save(User.builder()
                        .email(finalEmail)
                        .name(finalName)
                        .birth(finalBirth) // LocalDate 타입으로 전달
                        .phone(finalPhone) // String 타입으로 전달
                        .status(Status.ACTIVE)
                        .build());
            });
        } catch (Exception e) {
            log.error("### DB 저장 에러: {}", e.getMessage());
            response.sendRedirect("http://localhost:5173/login?error=db_error");
            return;
        }

        // 3. JWT 발행 및 리다이렉트 (수정된 부분)
        Map<String, Object> claims = Map.of("email", finalEmail, "role", "ROLE_USER");
        String accessToken = adminJWTUtil.generateUserAccessToken(claims);
        String refreshToken = adminJWTUtil.generateUserRefreshToken(claims);

// ⭐ build() 다음에 encode()를 추가해야 한글(name)이 안전하게 변환됩니다.
        String targetUrl = UriComponentsBuilder.fromUriString("http://localhost:5173/oauth-redirect")
                .queryParam("accessToken", accessToken)
                .queryParam("refreshToken", refreshToken)
                .queryParam("name", finalName)
                .queryParam("email", finalEmail)
                .build()
                .encode()
                .toUriString();

        log.info("### 리다이렉트 URL: {}", targetUrl);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}