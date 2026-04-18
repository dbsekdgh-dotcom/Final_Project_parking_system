package com.example.demo.domain.auth.admin.dtos.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AdminLoginRequest {
    private String loginId;
    private String password;
}
