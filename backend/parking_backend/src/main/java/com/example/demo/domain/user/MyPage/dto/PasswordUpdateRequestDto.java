package com.example.demo.domain.user.MyPage.dto;

import lombok.Getter;
import lombok.Setter;

@Setter//Jackson이 값 넣을 때 Setter 사용
@Getter
public class PasswordUpdateRequestDto {
    private String password;
}
