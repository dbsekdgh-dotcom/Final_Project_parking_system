package com.example.demo.domain.user.auth.dtos.response;

import com.example.demo.domain.shared.user.User;
import com.example.demo.domain.user.entity.SocialAccount;
import com.example.demo.domain.user.enums.Provider;
import com.example.demo.domain.shared.user.enums.Status;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter; // [추가] 이게 없으면 에러납니다.
import java.util.Map;

@Getter
public class OAuthAttributes {
    private Map<String, Object> attributes;
    private String nameAttributeKey;
    private String providerId; // [필수] 이 필드가 선언되어 있어야 합니다.
    private String name;
    private String email;
    private String phone;
    private LocalDate birth;   // [필수] 이 필드가 선언되어 있어야 합니다.
    private Provider provider;

    @Builder
    public OAuthAttributes(Map<String, Object> attributes, String nameAttributeKey, String providerId,
                           String name, String email, String phone, LocalDate birth, Provider provider) {
        this.attributes = attributes;
        this.nameAttributeKey = nameAttributeKey;
        this.providerId = providerId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.birth = birth;
        this.provider = provider;
    }

    public static OAuthAttributes extractOAuthAttributes(String registrationId, String userNameAttributeName, Map<String, Object> attributes) {
        if ("naver".equals(registrationId)) {
            return extractFromNaver(userNameAttributeName, attributes);
        }
        return extractFromKakao(userNameAttributeName, attributes);
    }

    private static OAuthAttributes extractFromNaver(String userNameAttributeName, Map<String, Object> attributes) {
        Map<String, Object> response = (Map<String, Object>) attributes.get("response");

        String year = (String) response.get("birthyear");
        String day = (String) response.get("birthday");
        LocalDate parsedBirth;
        try {
            if (year != null && day != null) {
                parsedBirth = LocalDate.parse(year + "-" + day, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            } else {
                parsedBirth = LocalDate.of(1900, 1, 1);
            }
        } catch (Exception e) {
            parsedBirth = LocalDate.of(1900, 1, 1);
        }

        return OAuthAttributes.builder()
                .name((String) response.get("name"))
                .email((String) response.get("email"))
                .phone((String) response.get("mobile"))
                .birth(parsedBirth)
                .provider(Provider.NAVER)
                .attributes(attributes)
                .nameAttributeKey(userNameAttributeName)
                .providerId((String) response.get("id"))
                .build();
    }

    private static OAuthAttributes extractFromKakao(String userNameAttributeName, Map<String, Object> attributes) {
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");

        return OAuthAttributes.builder()
                .name((String) profile.get("nickname"))
                .email((String) kakaoAccount.get("email"))
                .phone((String) kakaoAccount.get("phone_number"))
                .birth(LocalDate.of(1990, 1, 1))
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
                .birth(this.birth) // [복구] LocalDate.now() 대신 파싱된 birth 사용
                .build();
    }

    // [필수] CustomOAuth2UserService에서 사용하는 메서드
    public SocialAccount toSocialAccountEntity(User user) {
        return SocialAccount.builder()
                .user(user)
                .provider(this.provider)
                .providerId(this.providerId)
                .build();
    }
}