package com.example.demo.domain.user.auth.dtos.response;

import com.example.demo.domain.shared.user.User;
import com.example.demo.domain.user.enums.Provider;
import com.example.demo.domain.shared.user.enums.Status;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.Map;

@Getter
public class OAuthAttributes {
    private Map<String, Object> attributes;
    private String nameAttributeKey; // Spring Security용 키 이름 (예: "response", "id")
    private String providerId;       // DB 저장용 실제 고유 번호 (예: "234567...")
    private String name;
    private String email;
    private String phone;
    private Provider provider;

    @Builder
    public OAuthAttributes(Map<String, Object> attributes, String nameAttributeKey, String providerId,
                           String name, String email, String phone, Provider provider) {
        this.attributes = attributes;
        this.nameAttributeKey = nameAttributeKey;
        this.providerId = providerId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.provider = provider;
    }

    public static OAuthAttributes extractOAuthAttributes(String registrationId, String userNameAttributeName, Map<String, Object> attributes) {
        if ("naver".equals(registrationId)) {
            return extractFromNaver(userNameAttributeName, attributes);
        }
        return extractFromKakao(userNameAttributeName, attributes);
    }

    // 네이버 추출 로직 (수정 완료)
    private static OAuthAttributes extractFromNaver(String userNameAttributeName, Map<String, Object> attributes) {
        // 실제 데이터는 'response'라는 맵 안에 들어있음
        Map<String, Object> response = (Map<String, Object>) attributes.get("response");

        return OAuthAttributes.builder()
                .name((String) response.get("name"))
                .email((String) response.get("email"))
                .phone((String) response.get("mobile"))
                .provider(Provider.NAVER)
                /** * [중요] .attributes(attributes) 전체를 넣어야 합니다.
                 * Spring Security가 userNameAttributeName(response)을 이 맵에서 찾기 때문입니다.
                 */
                .attributes(attributes)
                .nameAttributeKey(userNameAttributeName)
                .providerId((String) response.get("id"))
                .build();
    }

    // 카카오 추출 로직 (수정 완료)
    private static OAuthAttributes extractFromKakao(String userNameAttributeName, Map<String, Object> attributes) {
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");

        return OAuthAttributes.builder()
                .name((String) profile.get("nickname"))
                .email((String) kakaoAccount.get("email"))
                .phone((String) kakaoAccount.get("phone_number"))
                .provider(Provider.KAKAO)
                .attributes(attributes)
                .nameAttributeKey(userNameAttributeName)
                .providerId(String.valueOf(attributes.get("id")))
                .build();
    }

    public User toUserEntity() {
        return User.builder()
                .name(this.name)
                .email(this.email)
                .phone(this.phone)
                .status(Status.ACTIVE)
                .birth(LocalDate.now())
                .build();
    }
}