package com.example.demo.domain.user.mypage.dashboard.dto.request;

import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Setter //Jackson이 값 넣을 때 Setter 사용
@Getter
public class MyPageDashboardUpdateRequestDto {
    private String name;
    private String phone;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate birth;
    private String password;
}
