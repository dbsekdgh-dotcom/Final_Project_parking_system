package com.example.demo.domain.user.auth.dtos.request;

import com.example.demo.domain.user.enums.Provider;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserSocialRecoverRequestDto {

    @NotBlank(message = "이메일은 필수 입력 항목입니다.")
    @Email(message = "유효한 이메일 형식이 아닙니다.")
    private String email;

    @NotNull(message = "소셜 제공자(Provider) 정보가 누락되었습니다.")
    private Provider provider;

    @NotBlank(message = "소셜 고유 ID(ProviderId)가 누락되었습니다.")
    private String providerId;
}
