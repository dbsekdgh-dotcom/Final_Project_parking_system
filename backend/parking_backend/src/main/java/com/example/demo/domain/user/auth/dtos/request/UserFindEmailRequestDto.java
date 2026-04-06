package com.example.demo.domain.user.auth.dtos.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserFindEmailRequestDto {
    private String name;
    private String phone;
}
