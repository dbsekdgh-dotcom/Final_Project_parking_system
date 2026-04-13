package com.example.demo.domain.user.mypage.dashboard.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class MyPageDashboardResponseDto {
    private String name;
    private String phone;
    private LocalDate birth;
    private String userStatus; // "일반 회원" | "입주민 신청 중" | "입주민"
}
