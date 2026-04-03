package com.example.demo.domain.user.auth.service;

import com.example.demo.domain.shared.user.User;
import com.example.demo.domain.user.auth.dtos.response.OAuthAttributes;
import com.example.demo.domain.user.auth.principal.PrincipalDetails;
import com.example.demo.domain.user.auth.repository.SocialAccountRepository;
import com.example.demo.domain.user.auth.repository.UserAuthRepository;
import com.example.demo.domain.user.entity.SocialAccount;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserAuthRepository userAuthRepository;
    private final SocialAccountRepository socialAccountRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // 1. 기본 OAuth2UserService를 통해 원본 유저 정보 로드
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        // 2. 서비스 구분 (kakao, naver 등)
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        // 3. 해당 서비스의 유저 식별 키값 (네이버는 보통 "response")
        String userNameAttributeName = userRequest.getClientRegistration()
                .getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();

        // 4. 추출된 데이터를 공통 DTO(OAuthAttributes)로 변환
        OAuthAttributes attributes = OAuthAttributes.extractOAuthAttributes(
                registrationId, userNameAttributeName, oAuth2User.getAttributes());

        // 5. 필수 정보인 이메일 체크
        if (attributes.getEmail() == null || attributes.getEmail().isBlank()) {
            log.error("### [소셜 에러] {} 서비스에서 이메일 획득 실패", registrationId);
            throw new OAuth2AuthenticationException(new OAuth2Error("email_not_found"), "이메일 정보가 필요합니다.");
        }

        try {
            // 6. DB 저장 또는 업데이트 (핵심 로직)
            User user = saveOrUpdate(attributes);
            log.info("### [소셜 로그인 완료] 처리된 계정: {}", user.getEmail());

            // 7. 인증 완료된 객체 반환
            return new PrincipalDetails(user, attributes.getAttributes());
        } catch (Exception e) {
            log.error("### [소셜 처리 에러] 서버 내부 오류: ", e);
            throw new OAuth2AuthenticationException(new OAuth2Error("social_processing_error"), "로그인 처리 중 오류가 발생했습니다.");
        }
    }

    /**
     * 핵심 수정 포인트:
     * getNameAttributeKey()는 네이버의 경우 "response"라는 문자열을 반환할 수 있으므로,
     * 실제 고유 ID 값인 getProviderId()를 사용하여 조회 및 저장해야 합니다.
     */
    private User saveOrUpdate(OAuthAttributes attributes) {
        // A. 기존 연동 정보가 있는지 '진짜 소셜 ID'로 조회
        return socialAccountRepository.findByProviderAndProviderId(
                        attributes.getProvider(), attributes.getProviderId()) // ⭐ 수정됨
                .map(SocialAccount::getUser)
                .orElseGet(() -> {
                    // B. 연동 정보가 없다면 이메일로 기존 유저 확인
                    User user = userAuthRepository.findByEmail(attributes.getEmail())
                            .orElseGet(() -> userAuthRepository.save(attributes.toUserEntity()));

                    // C. 새로운 소셜 연동 정보 생성 및 저장
                    SocialAccount newSocialConnection = SocialAccount.builder()
                            .user(user)
                            .provider(attributes.getProvider())
                            .providerId(attributes.getProviderId()) // ⭐ 수정됨
                            .build();

                    socialAccountRepository.save(newSocialConnection);
                    return user;
                });
    }
}