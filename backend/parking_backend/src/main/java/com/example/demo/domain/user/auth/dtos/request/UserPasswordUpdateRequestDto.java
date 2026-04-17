package com.example.demo.domain.user.auth.dtos.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "비밀번호 변경 요청 데이터 (로그인 상태에서 변경)")
public class UserPasswordUpdateRequestDto {

    @Schema(description = "새 비밀번호 (8자 이상)", example = "123123123")
    @NotBlank(message = "비밀번호를 입력해주세요.")
    @Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다.")
    private String password;

    @Schema(description = "새 비밀번호 확인", example = "123123123")
    @NotBlank(message = "비밀번호 확인을 입력해주세요.")
    private String passwordConfirm;
}