package com.example.demo.domain.auth.user.dtos.request;

import com.example.demo.domain.auth.user.enums.Provider;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "소셜 계정 복구 요청 데이터 (탈퇴 계정 재활성화)")
public class UserSocialRecoverRequestDto {

    @Schema(description = "복구할 소셜 계정의 이메일 주소", example = "hgd1234@test.com")
    @NotBlank(message = "이메일은 필수 입력 항목입니다.")
    @Email(message = "유효한 이메일 형식이 아닙니다.")
    private String email;

    @Schema(description = "소셜 로그인 제공자", example = "KAKAO")
    @NotNull(message = "소셜 제공자(Provider) 정보가 누락되었습니다.")
    private Provider provider;

    @NotBlank(message = "소셜 고유 ID(ProviderId)가 누락되었습니다.")
    private String providerId;
}
