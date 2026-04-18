package com.example.demo.domain.auth.user.dtos.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UserLoginResponseDto {

    private String code;         // null = 정상 로그인, "WITHDRAWN_ACCOUNT" = 탈퇴 계정
    private Long userId;
    private String email;
    private String name;
    private String userStatus;   // "NONE" | "PENDING" | "RESIDENT"
    private Integer unitNo;      // RESIDENT인 경우 호수, 나머지는 null
}