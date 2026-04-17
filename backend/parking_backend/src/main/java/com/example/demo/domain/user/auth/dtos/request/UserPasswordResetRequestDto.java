package com.example.demo.domain.user.auth.dtos.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "비밀번호 재설정 요청 데이터 (인증 완료 후 새 비밀번호 설정)")
public class UserPasswordResetRequestDto {

    @Schema(description = "비밀번호를 재설정할 이메일 주소", example = "hgd1234@test.com")
    @NotBlank(message = "이메일 정보가 누락되었습니다.")
    private String email;

    @Schema(description = "새 비밀번호 (8자 이상)", example = "123123123")
    @NotBlank(message = "새 비밀번호를 입력해주세요.")
    @Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다.")
    private String newPassword;

    @Schema(description = "새 비밀번호 확인", example = "123123123")
    @NotBlank(message = "비밀번호 확인을 입력해주세요.")
    private String confirmPassword;
}
