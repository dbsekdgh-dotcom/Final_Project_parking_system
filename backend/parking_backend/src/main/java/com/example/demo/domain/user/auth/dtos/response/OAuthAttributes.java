package com.example.demo.domain.user.auth.dtos.response;

import com.example.demo.domain.shared.user.User;
import com.example.demo.domain.user.enums.Provider;
import com.example.demo.domain.user.enums.Status;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.Map;

@Getter
public class OAuthAttributes {
    private Map<String, Object> attributes;
    private String nameAttributeKey;
    private String name;
    private String email;
    private String phone;
    private Provider provider;

    @Builder
    public OAuthAttributes(Map<String, Object> attributes, String nameAttributeKey,
                           String name, String email, String phone, Provider provider) {
        this.attributes = attributes;
        this.nameAttributeKey = nameAttributeKey;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.provider = provider;
    }

    // [번역기] 서비스별(naver/kakao) 추출 메서드 호출
    public static OAuthAttributes extractOAuthAttributes(String registrationId, String userNameAttributeName, Map<String, Object> attributes) {
        if ("naver".equals(registrationId)) {
            return extractFromNaver(userNameAttributeName, attributes);
        }
        return extractFromKakao(userNameAttributeName, attributes);
    }

    // 네이버에서 데이터 뽑기 (전부 다 들어온다고 가정)
    private static OAuthAttributes extractFromNaver(String userNameAttributeName, Map<String, Object> attributes) {
        Map<String, Object> response = (Map<String, Object>) attributes.get("response");

        return OAuthAttributes.builder()
                .name((String) response.get("name"))
                .email((String) response.get("email"))
                .phone((String) response.get("mobile"))
                .provider(Provider.NAVER)
                .attributes(response)
                .nameAttributeKey((String) response.get(userNameAttributeName))
                .build();
    }

    // 카카오에서 데이터 뽑기 (전부 다 들어온다고 가정)
    private static OAuthAttributes extractFromKakao(String userNameAttributeName, Map<String, Object> attributes) {
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");

        return OAuthAttributes.builder()
                .name((String) profile.get("nickname"))
                .email((String) kakaoAccount.get("email"))
                .phone((String) kakaoAccount.get("phone_number"))
                .provider(Provider.KAKAO)
                .attributes(attributes)
                .nameAttributeKey(String.valueOf(attributes.get(userNameAttributeName)))
                .build();
    }

    /**
     * [엔티티 변환] 소셜 데이터를 우리 User 엔티티 빌더에 그대로 넣습니다.
     * birth 등 소셜에서 직접 안 주는 값만 일단 예시로 넣어두었습니다.
     */
    public User toUserEntity() {
        return User.builder()
                .name(this.name)
                .email(this.email)
                .phone(this.phone)
                .status(Status.ACTIVE)
                // birth는 소셜 표준 응답에 없으므로, 엔티티의 NOT NULL을 피하기 위해
                // 일단 오늘 날짜나 특정 날짜로 빌더를 완성합니다.
                .birth(LocalDate.now())
                .build();
    }
}