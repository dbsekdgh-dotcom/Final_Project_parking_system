package com.example.demo.domain.user.auth.dtos.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UserLoginResponseDto {

    private String accessToken;
    private String refreshToken;
    private String email;
    private String name;
}
