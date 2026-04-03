package com.example.demo.domain.user.mypage.dashboard.dto.request;

import lombok.Getter;
import lombok.Setter;

@Setter//Jackson이 값 넣을 때 Setter 사용
@Getter
public class MyPageDashboardPasswordUpdateRequestDto {
    private String password;
}
