package com.example.demo.domain.user.auth.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserWithdrawRequestDto {
    @NotBlank(message = "비밀번호를 입력해주세요.")
    @Size(min = 9, message = "비밀번호는 최소 8자 이상이어야 합니다.")
    private String password;
    @NotBlank(message = "비밀번호 확인을 입력해주세요.")
    private String confirmPassword;
    @NotBlank(message = "탈퇴 확인 문구를 입력해주세요.")
    private String confirmText;
}
