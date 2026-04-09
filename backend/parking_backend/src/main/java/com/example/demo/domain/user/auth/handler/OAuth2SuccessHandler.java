package com.example.demo.domain.user.auth.handler;

import com.example.demo.domain.shared.user.User;
import com.example.demo.domain.shared.user.enums.Status;
import com.example.demo.domain.user.auth.service.UserVerificationService; // ⭐ 추가
import com.example.demo.global.util.admin.AdminJWTUtil;
import com.example.demo.domain.user.auth.repository.UserAuthRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final AdminJWTUtil adminJWTUtil;
    private final UserAuthRepository userAuthRepository;
    private final UserVerificationService userVerificationService; // ⭐ Redis 저장을 위한 주입 추가

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        Map<String, Object> attributes = oAuth2User.getAttributes();

        // 1. 소셜 서비스 구분 및 고유 ID 추출
        String provider = ((OAuth2AuthenticationToken) authentication).getAuthorizedClientRegistrationId().toUpperCase();
        String providerId = extractProviderId(provider, attributes);

        log.info("### 소셜 로그인 시도 [{}]: ID={}, Attributes={}", provider, providerId, attributes);

        String email = null;
        String name = "Social User";
        LocalDate birthDate = LocalDate.of(1900, 1, 1);
        String phone = "010-0000-0000";

        // 2. 서비스별 정보 추출
        try {
            if (provider.equals("NAVER")) {
                Map<String, Object> navResponse = (Map<String, Object>) attributes.get("response");
                if (navResponse != null) {
                    email = (String) navResponse.get("email");
                    name = (String) navResponse.get("name");
                    String mobile = (String) navResponse.get("mobile");
                    if (mobile != null) phone = mobile;

                    String year = (String) navResponse.get("birthyear");
                    String day = (String) navResponse.get("birthday");
                    if (year != null && day != null) {
                        birthDate = LocalDate.parse(year + "-" + day);
                    }
                }
            } else if (provider.equals("KAKAO")) {
                if (attributes.containsKey("email")) {
                    email = (String) attributes.get("email");
                    name = (String) attributes.get("nickname");
                } else {
                    Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
                    if (kakaoAccount != null) {
                        email = (String) kakaoAccount.get("email");
                        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
                        if (profile != null) name = (String) profile.get("nickname");
                    }
                }
            }
        } catch (Exception e) {
            log.error("### 데이터 추출 중 오류: {}", e.getMessage());
        }

        if (email == null) {
            response.sendRedirect("http://localhost:5202/?error=email_not_found");
            return;
        }

        // 3. DB 조회 및 탈퇴(DELETED) 상태 체크
        User user = userAuthRepository.findByEmail(email).orElse(null);

        if (user != null && user.getStatus() == Status.DELETED) {
            log.info("### [탈퇴 유저 감지] 복구 URL 생성: email={}, providerId={}", email, providerId);

            String recoveryUrl = UriComponentsBuilder.fromUriString("http://localhost:5202/")
                    .queryParam("error", "WITHDRAWN")
                    .queryParam("email", email)
                    .queryParam("provider", provider)
                    .queryParam("providerId", providerId)
                    .build()
                    .encode()
                    .toUriString();

            getRedirectStrategy().sendRedirect(request, response, recoveryUrl);
            return;
        }

        // 4. 신규 유저 자동 가입
        if (user == null) {
            user = userAuthRepository.save(User.builder()
                    .email(email)
                    .name(name)
                    .birth(birthDate)
                    .phone(phone)
                    .status(Status.ACTIVE)
                    .build());
        }

        // 5. 정상 유저 JWT 발행 및 ⭐ Redis 저장
        Map<String, Object> claims = Map.of(
                "email", email,
                "userId",user.getUserId(),
                "role", "USER" // 컨트롤러에서 쓰는 role 값과 통일
        );
        String accessToken = adminJWTUtil.generateUserAccessToken(claims);
        String refreshToken = adminJWTUtil.generateUserRefreshToken(claims);

        // ⭐ [핵심 추가] 소셜 로그인 성공 시 생성된 리프레시 토큰을 Redis에 저장
        userVerificationService.saveRefreshToken(email, refreshToken);
        log.info("### 소셜 로그인 성공 및 Redis RT 저장 완료: {}", email);

        String targetUrl = UriComponentsBuilder.fromUriString("http://localhost:5202/oauth-redirect")
                .queryParam("accessToken", accessToken)
                .queryParam("refreshToken", refreshToken)
                .queryParam("name", name)
                .queryParam("email", email)
                .build()
                .encode()
                .toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    private String extractProviderId(String provider, Map<String, Object> attributes) {
        if ("KAKAO".equals(provider)) {
            Object id = attributes.get("id");
            return id != null ? String.valueOf(id) : null;
        } else if ("NAVER".equals(provider)) {
            Map<String, Object> navResponse = (Map<String, Object>) attributes.get("response");
            if (navResponse != null) {
                return (String) navResponse.get("id");
            }
        }
        return null;
    }
}