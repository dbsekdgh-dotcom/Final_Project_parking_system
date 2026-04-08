package com.example.demo.domain.user.auth.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserFindResponseDto {

    private String email;

    // Boolean(래퍼타입): Lombok이 getIsWithdrawn()을 생성 → Jackson이 isWithdrawn으로 직렬화
    private Boolean isWithdrawn;
}