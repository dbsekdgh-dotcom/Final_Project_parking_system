package com.example.demo.domain.auth.user.dtos.response;

import com.example.demo.domain.resident.User;
import com.example.demo.domain.auth.user.enums.Provider;
import com.example.demo.domain.resident.enums.Status;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.Map;

@Slf4j
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

    /**
     * 소셜 서비스별로 데이터를 추출합니다.
     */
    public static OAuthAttributes extractOAuthAttributes(String registrationId, String userNameAttributeName, Map<String, Object> attributes) {
        if ("naver".equals(registrationId)) {
            return extractFromNaver(userNameAttributeName, attributes);
        }
        return extractFromKakao(userNameAttributeName, attributes);
    }

    /**
     * 네이버 정보 추출
     */
    private static OAuthAttributes extractFromNaver(String userNameAttributeName, Map<String, Object> attributes) {
        Map<String, Object> response = (Map<String, Object>) attributes.get("response");

        String year = (String) response.get("birthyear");
        String day = (String) response.get("birthday");
        LocalDate parsedBirth = LocalDate.of(1900, 1, 1);

        try {
            if (year != null && day != null) {
                parsedBirth = LocalDate.parse(year + "-" + day);
            }
        } catch (Exception e) {
            log.warn("### [Naver] 생년월일 파싱 실패: {} {}", year, day);
        }

        return OAuthAttributes.builder()
                .name((String) response.get("name"))
                .email((String) response.get("email"))
                .phone((String) response.get("mobile"))
                .birth(parsedBirth)
                .provider(Provider.NAVER)
                .attributes(attributes) // 전체 attributes 유지
                .nameAttributeKey(userNameAttributeName)
                .providerId((String) response.get("id"))
                .build();
    }

    /**
     * 카카오 정보 추출
     */
    private static OAuthAttributes extractFromKakao(String userNameAttributeName, Map<String, Object> attributes) {
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");

        // 생년월일 처리
        String birthyear = (String) kakaoAccount.get("birthyear");
        String birthday = (String) kakaoAccount.get("birthday");
        LocalDate parsedBirth = LocalDate.of(1900, 1, 1);

        try {
            if (birthyear != null && birthday != null) {
                String formattedBirth = birthyear + "-" + birthday.substring(0, 2) + "-" + birthday.substring(2);
                parsedBirth = LocalDate.parse(formattedBirth);
            }
        } catch (Exception e) {
            log.warn("### [Kakao] 생년월일 파싱 실패: {} {}", birthyear, birthday);
        }

        // 전화번호 포맷팅
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
                .providerId(String.valueOf(attributes.get("id"))) // Long 타입을 String으로 안전하게 변환
                .build();
    }

    /**
     * 전화번호 포맷팅 유틸리티 (010-XXXX-XXXX)
     */
    private static String formatPhoneNumber(String phoneNumber) {
        if (phoneNumber == null) return "010-0000-0000";

        String cleaned = phoneNumber.replaceAll("[^0-9]", "");
        if (cleaned.startsWith("82")) {
            cleaned = "0" + cleaned.substring(2);
        }

        if (cleaned.length() == 11) {
            return cleaned.replaceFirst("(\\d{3})(\\d{4})(\\d{4})", "$1-$2-$3");
        } else if (cleaned.length() == 10) {
            return cleaned.replaceFirst("(\\d{3})(\\d{3})(\\d{4})", "$1-$2-$3");
        }
        return cleaned;
    }

    /**
     * User 엔티티로 변환
     */
    public User toUserEntity() {
        return User.builder()
                .name(this.name)
                .email(this.email)
                .phone(this.phone)
                .status(Status.ACTIVE)
                .birth(this.birth)
                .build();
    }

}