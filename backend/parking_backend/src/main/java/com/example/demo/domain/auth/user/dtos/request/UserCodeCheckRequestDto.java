package com.example.demo.domain.auth.user.dtos.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "이메일 인증번호 확인 요청 데이터")
public class UserCodeCheckRequestDto {

    @Schema(description = "인증번호를 받은 이메일 주소", example = "hgd1234@test.com")
    @NotBlank(message = "이메일은 필수 입력 항목입니다.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    private String email;

    @Schema(description = "이메일로 수신한 6자리 인증번호", example = "123456")
    @NotBlank(message = "인증번호를 입력해주세요.")
    private String code;
}
