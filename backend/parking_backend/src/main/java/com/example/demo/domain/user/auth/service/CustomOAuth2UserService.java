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
import org.springframework.security.oauth2.core.OAuth2Error;
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
        // 1. 기본 OAuth2UserService를 통해 사용자 정보를 가져옴
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        // 2. 서비스 구분 (google, kakao 등)
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        // 3. OAuth2 로그인 진행 시 키가 되는 필드값 (Primary Key 역할)
        String userNameAttributeName = userRequest.getClientRegistration()
                .getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();

        // 4. 소셜별로 다른 데이터를 공통 규격(OAuthAttributes)으로 추출
        OAuthAttributes attributes = OAuthAttributes.extractOAuthAttributes(
                registrationId, userNameAttributeName, oAuth2User.getAttributes());

        // [추가] 5. 필수 정보 검증 (예: 사용자가 이메일 제공을 거부한 경우)
        if (attributes.getEmail() == null || attributes.getEmail().isBlank()) {
            log.error("소셜 로그인 에러: {} 서비스에서 이메일 정보를 불러올 수 없습니다.", registrationId);
            throw new OAuth2AuthenticationException(new OAuth2Error("email_not_found"), "이메일 정보가 필요합니다.");
        }

        try {
            // 6. DB 저장 또는 업데이트
            User user = saveOrUpdate(attributes);

            return new DefaultOAuth2User(
                    Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
                    attributes.getAttributes(),
                    attributes.getNameAttributeKey()
            );
        } catch (Exception e) {
            log.error("소셜 계정 처리 중 서버 에러 발생: ", e);
            throw new OAuth2AuthenticationException(new OAuth2Error("social_processing_error"), "소셜 로그인 처리 중 오류가 발생했습니다.");
        }
    }

    private User saveOrUpdate(OAuthAttributes attributes) {
        // 이미 연결된 소셜 계정이 있는지 확인
        return socialAccountRepository.findByProviderAndProviderId(
                        attributes.getProvider(), attributes.getNameAttributeKey())
                .map(socialAccount -> socialAccount.getUser()) // 있다면 해당 유저 반환
                .orElseGet(() -> {
                    // 없다면 이메일로 기존 유저를 찾거나, 아예 새로 생성(회원가입)
                    User user = userAuthRepository.findByEmail(attributes.getEmail())
                            .orElseGet(() -> userAuthRepository.save(attributes.toUserEntity()));

                    // 신규 소셜 연결 정보 저장
                    SocialAccount newSocialConnection = SocialAccount.builder()
                            .user(user)
                            .provider(attributes.getProvider())
                            .providerId(attributes.getNameAttributeKey())
                            .build();
                    socialAccountRepository.save(newSocialConnection);
                    return user;
                });
    }
}