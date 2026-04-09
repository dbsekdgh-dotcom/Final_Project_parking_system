package com.example.demo.domain.user.auth.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRecoverRequestDto {
    @NotBlank(message = "이메일은 필수입니다.")
    private String email;

    @NotBlank(message = "인증번호를 입력해주세요.")
    private String authCode;

    @NotBlank(message = "새 비밀번호를 입력해주세요.")
    @Size(min = 8)
    private String newPassword;

    @NotBlank(message = "비밀번호 확인을 입력해주세요.")
    private String passwordConfirm;

    private String name;
    private String phone;

}