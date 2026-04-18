package com.example.demo.domain.auth.user.dtos.request;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "비밀번호 재설정 최종 요청 데이터 (인증번호 확인 + 새 비밀번호 설정)")
public class UserRecoverRequestDto {

    @Schema(description = "비밀번호를 재설정할 이메일 주소", example = "hgd1234@test.com")
    @NotBlank(message = "이메일은 필수입니다.")
    private String email;

    @Schema(description = "이메일로 수신한 6자리 인증번호", example = "123456")
    @NotBlank(message = "인증번호를 입력해주세요.")
    private String authCode;

    @Schema(description = "새 비밀번호 (8자 이상)", example = "123123123")
    @NotBlank(message = "새 비밀번호를 입력해주세요.")
    @Size(min = 8)
    private String newPassword;

    @Schema(description = "새 비밀번호 확인", example = "123123123")
    @NotBlank(message = "비밀번호 확인을 입력해주세요.")
    private String passwordConfirm;

    @Schema(description = "가입 시 등록한 실명 (선택 — 추가 본인 확인용)", example = "홍길동")
    private String name;

    @Schema(description = "가입 시 등록한 휴대폰 번호 (선택 — 추가 본인 확인용)", example = "010-1111-1234")
    private String phone;

}