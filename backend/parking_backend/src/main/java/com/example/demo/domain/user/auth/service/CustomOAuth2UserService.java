package com.example.demo.domain.user.auth.service;

import com.example.demo.domain.user.auth.dtos.response.OAuthAttributes;
import com.example.demo.domain.user.auth.repository.SocialAccountRepository;
import com.example.demo.domain.user.auth.repository.UserAuthRepository;
import com.example.demo.domain.user.entity.SocialAccount;
import com.example.demo.domain.shared.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserAuthRepository userAuthRepository;
    private final SocialAccountRepository socialAccountRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String userNameAttributeName = userRequest.getClientRegistration()
                .getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();

        // [수정 포인트] 위에서 만든 OAuthAttributes.extractOAuthAttributes 사용
        OAuthAttributes attributes = OAuthAttributes.extractOAuthAttributes(
                registrationId, userNameAttributeName, oAuth2User.getAttributes());

        User user = saveOrUpdate(attributes);

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
                attributes.getAttributes(),
                attributes.getNameAttributeKey()
        );
    }

    private User saveOrUpdate(OAuthAttributes attributes) {
        // [수정 포인트] attributes.getProviderId()를 사용하여 실제 고유값으로 조회
        return socialAccountRepository.findByProviderAndProviderId(
                        attributes.getProvider(), attributes.getProviderId())
                .map(SocialAccount::getUser)
                .orElseGet(() -> {
                    // 이메일로 기존 유저 확인 또는 신규 가입
                    User user = userAuthRepository.findByEmail(attributes.getEmail())
                            .orElseGet(() -> userAuthRepository.save(attributes.toUserEntity()));

                    // [수정 포인트] SocialAccount 생성 시 providerId 필드에 실제 고유값 할당
                    // 빨간 줄이 났던 connectedAt은 엔티티 설정에 따라 제외하거나 이름을 확인해야 합니다.
                    SocialAccount newSocialConnection = SocialAccount.builder()
                            .user(user)
                            .provider(attributes.getProvider())
                            .providerId(attributes.getProviderId())
                            // 만약 엔티티에 connectedAt 필드가 확실히 있다면 아래 주석을 푸세요.
                            // .connectedAt(java.time.LocalDateTime.now())
                            .build();

                    socialAccountRepository.save(newSocialConnection);
                    return user;
                });
    }
}