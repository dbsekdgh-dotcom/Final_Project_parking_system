package com.example.demo.domain.user.mypage.dashboard.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
//setter필요없음, 우리가 직접 생성해서 반환함
//외부에서 값 바꿀 필요 없음 (불변 객체)
public class MyPageDashboardResponseDto {
    private String name;
    private String phone;
    private LocalDate birth;
}
