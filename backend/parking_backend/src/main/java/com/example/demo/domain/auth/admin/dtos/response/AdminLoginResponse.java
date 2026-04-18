package com.example.demo.domain.auth.admin.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminLoginResponse {
    private String accessToken;
    private String adminName; // 화면 상단에 "ㅇㅇㅇ관리자님" 표시용
    private String loginId; // 관리자 식별용
}
