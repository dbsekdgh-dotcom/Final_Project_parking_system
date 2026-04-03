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

    // 회원가입 및 기존 유저 조회를 위한 리포지토리
    private final UserAuthRepository userAuthRepository;

    // 유저와 소셜(카카오/네이버) 계정 사이의 연결 정보를 저장하는 리포지토리
    private final SocialAccountRepository socialAccountRepository;

    @Override
    @Transactional // DB 작업 도중 에러가 나면 모든 변경사항을 취소(Rollback)함
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        // 1. 소셜 서버(카카오 등)와 통신하여 유저 정보를 가져올 기본 도구를 준비함
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();

        // 2. 실제 소셜 서버로부터 원본 유저 프로필 데이터(Attributes)를 받아옴
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        // 3. 현재 로그인 시도 중인 서비스 이름 (예: "kakao", "naver")을 가져옴
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        // 4. 소셜 서비스별로 사용자를 식별하는 고유 PK 키값의 이름(카카오는 보통 "id")을 가져옴
        String userNameAttributeName = userRequest.getClientRegistration()
                .getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();

        // 5. 제각각인 소셜 데이터를 우리 프로젝트 공통 규격(OAuthAttributes)으로 변환/추출함
        OAuthAttributes attributes = OAuthAttributes.extractOAuthAttributes(
                registrationId, userNameAttributeName, oAuth2User.getAttributes());

        // 6. [방어 코드] 필수 정보인 이메일이 누락된 경우, 로그인을 중단하고 에러를 던짐
        if (attributes.getEmail() == null || attributes.getEmail().isBlank()) {
            log.error("### [소셜 에러] {} 서비스에서 이메일 획득 실패", registrationId);
            throw new OAuth2AuthenticationException(new OAuth2Error("email_not_found"), "이메일 정보가 필요합니다.");
        }

        try {
            // 7. 가입 여부 확인 및 자동 회원가입 진행 (회색 변수 방지를 위해 로그에 활용)
            User user = saveOrUpdate(attributes);

            // 8. DB 처리가 완료된 유저의 이메일을 로그에 찍어 처리 흐름을 확인 (회색 변수 해결)
            log.info("### [소셜 로그인 완료] 처리된 계정: {}", user.getEmail());

            // 9. 시큐리티 인증이 완료된 최종 유저 객체를 반환 (이후 SuccessHandler로 이동)
            return new DefaultOAuth2User(
                    Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")), // 유저 권한 설정
                    attributes.getAttributes(), // 소셜 원본 데이터 전달
                    attributes.getNameAttributeKey() // 식별자 키값 전달
            );
        } catch (Exception e) {
            // DB 저장 등 처리 과정에서 발생하는 모든 에러를 로그로 남김
            log.error("### [소셜 처리 에러] 서버 내부 오류: ", e);
            throw new OAuth2AuthenticationException(new OAuth2Error("social_processing_error"), "로그인 처리 중 오류가 발생했습니다.");
        }
    }

    /**
     * 소셜 정보를 바탕으로 DB 가입 여부를 확인하고, 없으면 새로 저장하는 메서드
     */
    private User saveOrUpdate(OAuthAttributes attributes) {

        // A. 소셜 연결 테이블에서 "해당 서비스 + 고유 식별번호"로 가입된 이력이 있는지 조회
        return socialAccountRepository.findByProviderAndProviderId(
                        attributes.getProvider(), attributes.getNameAttributeKey())
                .map(socialAccount -> socialAccount.getUser()) // 이미 있다면 연결된 유저 정보를 반환
                .orElseGet(() -> {
                    // B. 연결 이력이 없다면, 이메일을 통해 기존 일반 가입 유저가 있는지 확인
                    User user = userAuthRepository.findByEmail(attributes.getEmail())
                            // C. 아예 처음 본 유저라면 새로 회원가입(User 생성) 진행
                            .orElseGet(() -> userAuthRepository.save(attributes.toUserEntity()));

                    // D. 새로 가입했거나 기존 유저인 경우, 이 소셜 계정과의 연결 고리(SocialAccount)를 생성하여 저장
                    SocialAccount newSocialConnection = SocialAccount.builder()
                            .user(user)
                            .provider(attributes.getProvider())
                            .providerId(attributes.getNameAttributeKey())
                            .build();

                    socialAccountRepository.save(newSocialConnection);
                    return user; // 최종 유저 정보 반환
                });
    }
}