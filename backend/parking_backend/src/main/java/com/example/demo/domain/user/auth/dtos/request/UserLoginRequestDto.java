package com.example.demo.domain.user.auth.dtos.request;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserLoginRequestDto {
    private String email;
    private String password;
}
