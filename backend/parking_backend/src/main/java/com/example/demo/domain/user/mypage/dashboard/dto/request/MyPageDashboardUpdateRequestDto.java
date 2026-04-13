package com.example.demo.domain.user.mypage.dashboard.dto.request;

import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Setter
@Getter
public class MyPageDashboardUpdateRequestDto {

    // 전화번호 - 이메일 인증 후 변경. null이면 수정 안함
    @Pattern(regexp = "^01[016789]-?\\d{3,4}-?\\d{4}$", message = "올바른 전화번호 형식이 아닙니다. (예: 010-1234-5678)")
    private String phone;

    // 생일 - 직접 수정. null이면 수정 안함
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate birth;
}
