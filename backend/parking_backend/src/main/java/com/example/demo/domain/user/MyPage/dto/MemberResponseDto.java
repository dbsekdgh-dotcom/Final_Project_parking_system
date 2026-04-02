package com.example.demo.domain.user.MyPage.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
//setter필요없음, 우리가 직접 생성해서 반환함
//외부에서 값 바꿀 필요 없음 (불변 객체)
public class MemberResponseDto {
    private String name;
    private String phone;
    private LocalDate birth;
}
