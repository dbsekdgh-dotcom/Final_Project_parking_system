package com.example.demo.domain.user.auth.dtos.request;


import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class UserSignupRequestDto {

    private String email;
    private String password;
    private String name;
    private String phone;
    private LocalDate birth;
}
