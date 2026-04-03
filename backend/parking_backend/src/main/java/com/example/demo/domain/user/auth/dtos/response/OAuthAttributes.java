package com.example.demo.domain.user.auth.dtos.response;

import com.example.demo.domain.shared.user.User;
import com.example.demo.domain.user.entity.SocialAccount;
import com.example.demo.domain.user.enums.Provider;
import com.example.demo.domain.shared.user.enums.Status;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.Map;

@Getter
public class OAuthAttributes {
    private Map<String, Object> attributes;
    private String nameAttributeKey;
    private String providerId;
    private String name;
    private String email;
    private String phone;
    private LocalDate birth;
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

        // 생년월일 파싱 로직 (잘 작성됨)
        String year = (String) response.get("birthyear");
        String day = (String) response.get("birthday");
        LocalDate parsedBirth = (year != null && day != null)
                ? LocalDate.parse(year + "-" + day)
                : LocalDate.of(1900, 1, 1);

        return OAuthAttributes.builder()
                .name((String) response.get("name"))
                .email((String) response.get("email"))
                .phone((String) response.get("mobile"))
                .birth(parsedBirth)
                .provider(Provider.NAVER)
                // ⭐ 수정 포인트: attributes를 'response' 맵으로 교체하고,
                // nameAttributeKey를 "id"로 명시하는 것이 훨씬 안전합니다.
                .attributes(response)
                .nameAttributeKey("id")
                .providerId((String) response.get("id"))
                .build();
    }

    private static OAuthAttributes extractFromKakao(String userNameAttributeName, Map<String, Object> attributes) {
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");

        // 1. 생년월일 변환 (yyyy-MM-dd)
        String birthyear = (String) kakaoAccount.get("birthyear");
        String birthday = (String) kakaoAccount.get("birthday");
        LocalDate parsedBirth = LocalDate.of(1900, 1, 1);

        try {
            if (birthyear != null && birthday != null) {
                // 카카오 MMDD(0907) 형식을 yyyy-MM-dd로 변환
                String formattedBirth = birthyear + "-" + birthday.substring(0, 2) + "-" + birthday.substring(2);
                parsedBirth = LocalDate.parse(formattedBirth);
            }
        } catch (Exception e) {
            // 파싱 에러 시 기본값 유지
        }

        // 2. 전화번호 가공 (하이픈 추가: 010-XXXX-XXXX)
        String rawPhone = (String) kakaoAccount.get("phone_number");
        String formattedPhone = formatPhoneNumber(rawPhone);

        return OAuthAttributes.builder()
                .name((String) kakaoAccount.get("name"))
                .email((String) kakaoAccount.get("email"))
                .phone(formattedPhone)
                .birth(parsedBirth)
                .provider(Provider.KAKAO)
                .attributes(attributes)
                .nameAttributeKey(userNameAttributeName)
                .providerId(String.valueOf(attributes.get("id")))
                .build();
    }

    // 전화번호 포맷팅 유틸리티 메서드
    private static String formatPhoneNumber(String phoneNumber) {
        if (phoneNumber == null) return "010-0000-0000";

        // 숫자만 남기기 (+82 10-1234-5678 -> 821012345678)
        String cleaned = phoneNumber.replaceAll("[^0-9]", "");

        // 한국 국가번호(82)를 0으로 변환 (821012345678 -> 01012345678)
        if (cleaned.startsWith("82")) {
            cleaned = "0" + cleaned.substring(2);
        }

        // 하이픈 추가 (01012345678 -> 010-1234-5678)
        if (cleaned.length() == 11) {
            return cleaned.replaceFirst("(\\d{3})(\\d{4})(\\d{4})", "$1-$2-$3");
        } else if (cleaned.length() == 10) {
            return cleaned.replaceFirst("(\\d{3})(\\d{3})(\\d{4})", "$1-$2-$3");
        }

        return cleaned;
    }

    public User toUserEntity() {
        return User.builder()
                .name(this.name)
                .email(this.email)
                .phone(this.phone)
                .status(Status.ACTIVE)
                .birth(this.birth)
                .build();
    }

    public SocialAccount toSocialAccountEntity(User user) {
        return SocialAccount.builder()
                .user(user)
                .provider(this.provider)
                .providerId(this.providerId)
                .build();
    }
}