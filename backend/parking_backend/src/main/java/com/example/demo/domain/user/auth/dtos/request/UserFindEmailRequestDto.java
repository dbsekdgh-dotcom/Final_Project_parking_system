package com.example.demo.domain.user.auth.dtos.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "이메일 찾기 요청 데이터")
public class UserFindEmailRequestDto {

    @Schema(description = "가입 시 등록한 실명", example = "홍길동")
    @NotBlank(message = "이름은 필수 입력 항목입니다.")
    private String name;

    @Schema(description = "가입 시 등록한 휴대폰 번호", example = "010-1111-1234")
    @NotBlank(message = "전화번호는 필수 입력 항목입니다.")
    private String phone;
}
