package com.example.demo.domain.auth.user.service;

import com.example.demo.domain.resident.User;
import com.example.demo.domain.auth.user.dtos.response.OAuthAttributes;
import com.example.demo.global.util.admin.AdminJWTUtil;
import com.example.demo.domain.auth.user.principal.PrincipalDetails;
import com.example.demo.domain.auth.user.repository.SocialAccountRepository;
import com.example.demo.domain.auth.user.repository.UserAuthRepository;
import com.example.demo.domain.auth.user.entity.SocialAccount;
import com.example.demo.domain.auth.user.enums.Provider; // Enum 임포트 확인
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
    private final AdminJWTUtil adminJWTUtil;
    private final HttpServletRequest request;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // 1. 소셜 정보 가져오기
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        // 2. 서비스 추출 (kakao, naver 등)
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String userNameAttributeName = userRequest.getClientRegistration()
                .getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();

        OAuthAttributes attributes = OAuthAttributes.extractOAuthAttributes(
                registrationId, userNameAttributeName, oAuth2User.getAttributes());

        // 3. 이메일 체크
        if (attributes.getEmail() == null || attributes.getEmail().isBlank()) {
            throw new OAuth2AuthenticationException(new OAuth2Error("email_not_found"), "이메일 정보가 필요합니다.");
        }

        // 4. 기존 로그인 계정 검증
        validateUserMatchingWithCookie(attributes.getEmail());

        try {
            // 5. DB 저장 또는 업데이트
            User user = saveOrUpdate(attributes);
            return new PrincipalDetails(user, attributes.getAttributes());
        } catch (Exception e) {
            log.error("### [소셜 처리 에러] ", e);
            throw new OAuth2AuthenticationException(new OAuth2Error("social_processing_error"), "로그인 처리 중 서버 오류가 발생했습니다.");
        }
    }

    private void validateUserMatchingWithCookie(String socialEmail) {
        if (request.getCookies() != null) {
            Arrays.stream(request.getCookies())
                    .filter(cookie -> "temp_jwt".equals(cookie.getName()))
                    .findFirst()
                    .ifPresent(cookie -> {
                        try {
                            String loginUserEmail = adminJWTUtil.validateUserToken(cookie.getValue()).getSubject();
                            if (loginUserEmail != null && !loginUserEmail.equals(socialEmail)) {
                                throw new OAuth2AuthenticationException(new OAuth2Error("email_mismatch"));
                            }
                        } catch (Exception e) {
                            log.error("### [연동체크] 쿠키 토큰 검증 실패");
                        }
                    });
        }
    }

    /**
     * [해결 포인트]
     * 1. 람다 캡처링 방지를 위해 지역 변수로 값을 미리 추출 (final 사용)
     * 2. Repository 메서드 인자 타입(Enum)과 일치시킴
     */
    private User saveOrUpdate(final OAuthAttributes attributes) {
        // 람다 내부에서 사용할 변수들을 미리 고정(final)시킵니다.
        final Provider provider = attributes.getProvider();
        final String providerId = attributes.getProviderId();
        final String email = attributes.getEmail();

        // 1. 이미 연동된 소셜 계정이 있는지 조회
        return socialAccountRepository.findByProviderAndProviderId(provider, providerId)
                .map(socialAccount -> {
                    log.info("### [기존 연동 발견] {}", socialAccount.getUser().getEmail());
                    return socialAccount.getUser();
                })
                .orElseGet(() -> {
                    // 2. 소셜 정보는 없으나 기존 이메일 유저가 있는지 확인
                    User user = userAuthRepository.findByEmail(email)
                            .orElseGet(() -> {
                                log.info("### [신규 유저 생성] {}", email);
                                return userAuthRepository.save(attributes.toUserEntity());
                            });

                    // 3. 중복 인서트 방지 (찰나의 순간 재확인)
                    if (!socialAccountRepository.existsByProviderAndProviderId(provider, providerId)) {
                        log.info("### [신규 연동 저장] 유저: {}, 제공자: {}", user.getEmail(), provider);
                        SocialAccount newConnection = SocialAccount.builder()
                                .user(user)
                                .provider(provider)
                                .providerId(providerId)
                                .build();
                        socialAccountRepository.save(newConnection);
                    }
                    return user;
                });
    }
}