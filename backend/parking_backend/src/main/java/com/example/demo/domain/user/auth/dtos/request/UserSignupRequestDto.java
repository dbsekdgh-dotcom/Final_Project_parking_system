package com.example.demo.domain.user.auth.dtos.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Schema(description = "회원가입 요청 데이터")
public class UserSignupRequestDto {

    @Schema(description = "이메일 주소", example = "hgd1234@test.com")
    @NotBlank(message = "이메일은 필수 입력 항목입니다.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    private String email;

    @Schema(description = "비밀번호 (8자 이상)", example = "123123123")
    @NotBlank(message = "비밀번호를 입력해주세요.")
    @Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다.")
    private String password;

    @Schema(description = "사용자 실명", example = "홍길동")
    @NotBlank(message = "이름은 필수 입력 항목입니다.")
    private String name;

    @Schema(description = "휴대폰 번호", example = "010-1111-1234")
    @NotBlank(message = "전화번호는 필수 입력 항목입니다.")
    private String phone;

    @Schema(description = "생년월일", example = "1980-01-01")
    @NotNull(message = "생년월일은 필수 입력 항목입니다.")
    private LocalDate birth;
}