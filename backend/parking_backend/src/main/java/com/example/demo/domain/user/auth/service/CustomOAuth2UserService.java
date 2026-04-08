package com.example.demo.domain.user.auth.service;

import com.example.demo.domain.shared.user.User;
import com.example.demo.domain.user.auth.dtos.response.OAuthAttributes;
import com.example.demo.global.util.admin.AdminJWTUtil;
import com.example.demo.domain.user.auth.principal.PrincipalDetails;
import com.example.demo.domain.user.auth.repository.SocialAccountRepository;
import com.example.demo.domain.user.auth.repository.UserAuthRepository;
import com.example.demo.domain.user.entity.SocialAccount;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserAuthRepository userAuthRepository;
    private final SocialAccountRepository socialAccountRepository;
    private final AdminJWTUtil adminJWTUtil; // 쿠키 검증을 위해 주입
    private final HttpServletRequest request; // 현재 요청의 쿠키를 읽기 위해 주입

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // 1. 기본 서비스 호출하여 소셜 정보 가져오기
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        // 2. 서비스 구분 및 속성 추출
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String userNameAttributeName = userRequest.getClientRegistration()
                .getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();

        OAuthAttributes attributes = OAuthAttributes.extractOAuthAttributes(
                registrationId, userNameAttributeName, oAuth2User.getAttributes());

        // 3. 이메일 필수 체크
        if (attributes.getEmail() == null || attributes.getEmail().isBlank()) {
            log.error("### [소셜 에러] {} 서비스에서 이메일을 가져올 수 없습니다.", registrationId);
            throw new OAuth2AuthenticationException(new OAuth2Error("email_not_found"), "이메일 정보가 필요합니다.");
        }

        // 4. [핵심] 쿠키를 통한 현재 로그인 계정 검증 (필터 순서 문제 완전 해결)
        validateUserMatchingWithCookie(attributes.getEmail());

        try {
            // 5. DB 저장 또는 업데이트
            User user = saveOrUpdate(attributes);
            log.info("### [소셜 처리 완료] 계정: {}, 서비스: {}", user.getEmail(), registrationId);

            // 6. 최종 인증 객체 생성
            return new PrincipalDetails(user, attributes.getAttributes());
        } catch (OAuth2AuthenticationException e) {
            throw e;
        } catch (Exception e) {
            log.error("### [소셜 처리 에러] ", e);
            throw new OAuth2AuthenticationException(new OAuth2Error("social_processing_error"), "로그인 처리 중 서버 오류가 발생했습니다.");
        }
    }

    /**
     * 프론트엔드에서 보낸 temp_jwt 쿠키를 직접 확인하여 계정 일치 여부를 검증합니다.
     */
    private void validateUserMatchingWithCookie(String socialEmail) {
        String loginUserEmail = null;

        // 1. 현재 요청의 쿠키에서 temp_jwt 찾기
        if (request.getCookies() != null) {
            loginUserEmail = Arrays.stream(request.getCookies())
                    .filter(cookie -> "temp_jwt".equals(cookie.getName()))
                    .map(cookie -> {
                        try {
                            // JWTUtil을 사용하여 토큰에서 이메일 추출
                            return adminJWTUtil.validateUserToken(cookie.getValue()).getSubject();
                        } catch (Exception e) {
                            log.error("### [연동체크] 쿠키 토큰 검증 실패: {}", e.getMessage());
                            return null;
                        }
                    })
                    .filter(email -> email != null)
                    .findFirst()
                    .orElse(null);
        }

        // 2. 로그인된 상태(쿠키 존재)라면 이메일 비교
        if (loginUserEmail != null) {
            log.info("### [연동체크] 기존 로그인 계정: {}, 시도 중인 소셜 계정: {}", loginUserEmail, socialEmail);

            if (!loginUserEmail.equals(socialEmail)) {
                log.warn("### [보안 차단] 계정 불일치! 기존: {}, 신규: {}", loginUserEmail, socialEmail);
                // OAuth2SuccessHandler에서 이 에러를 받아 ?error=email_mismatch 로 리다이렉트합니다.
                throw new OAuth2AuthenticationException(
                        new OAuth2Error("email_mismatch"),
                        "현재 로그인된 계정 정보와 일치하는 소셜 계정만 연동할 수 있습니다."
                );
            }
            log.info("### [연동체크] 계정 일치 확인 완료.");
        }
    }

    private User saveOrUpdate(OAuthAttributes attributes) {
        return socialAccountRepository.findByProviderAndProviderId(
                        attributes.getProvider(), attributes.getProviderId())
                .map(SocialAccount::getUser)
                .orElseGet(() -> {
                    User user = userAuthRepository.findByEmail(attributes.getEmail())
                            .orElseGet(() -> userAuthRepository.save(attributes.toUserEntity()));

                    SocialAccount newConnection = SocialAccount.builder()
                            .user(user)
                            .provider(attributes.getProvider())
                            .providerId(attributes.getProviderId())
                            .build();

                    socialAccountRepository.save(newConnection);
                    return user;
                });
    }
}