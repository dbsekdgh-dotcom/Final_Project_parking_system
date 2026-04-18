package com.example.demo.domain.auth.user.dtos.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "회원 탈퇴 요청 데이터")
public class UserWithdrawRequestDto {

    @Schema(description = "현재 비밀번호 (8자 이상)", example = "123123123")
    @NotBlank(message = "비밀번호를 입력해주세요.")
    @Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다.")
    private String password;

    @Schema(description = "비밀번호 확인", example = "123123123")
    @NotBlank(message = "비밀번호 확인을 입력해주세요.")
    private String confirmPassword;

    @Schema(description = "탈퇴 의사 확인 문구 ('회원 탈퇴' 를 그대로 입력)", example = "회원 탈퇴")
    @NotBlank(message = "탈퇴 확인 문구를 입력해주세요.")
    private String confirmText;
}
