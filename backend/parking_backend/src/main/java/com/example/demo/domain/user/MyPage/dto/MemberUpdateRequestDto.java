package com.example.demo.domain.user.MyPage.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Setter //Jackson이 값 넣을 때 Setter 사용
@Getter
public class MemberUpdateRequestDto {
    private String name;
    private String phone;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate birth;
}
