package com.example.demo.domain.auth.user.dtos.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "비밀번호 재설정 인증번호 발송 요청 데이터")
public class UserRecoverSendCodeRequestDto {

    @Schema(description = "가입 시 등록한 실명", example = "홍길동")
    @NotBlank(message = "이름은 필수 입력 항목입니다.")
    private String name;

    @Schema(description = "가입한 이메일 주소 (인증번호 수신지)", example = "hgd1234@test.com")
    @NotBlank(message = "이메일은 필수 입력 항목입니다.")
    @Email(message = "유효한 이메일 형식이 아닙니다.")
    private String email;

    @Schema(description = "가입 시 등록한 휴대폰 번호", example = "010-1111-1234")
    @NotBlank(message = "전화번호는 필수 입력 항목입니다.")
    private String phone;
}