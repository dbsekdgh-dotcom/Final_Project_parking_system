package com.example.demo.domain.management.store.dtos.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class StoreActivateRequestDto {
    @NotBlank(message = "상가명을 입력해주세요.")
    private String name;
    @NotBlank(message = "단말기 비밀번호를 입력해주세요.")
    private String terminalPassword;
}
