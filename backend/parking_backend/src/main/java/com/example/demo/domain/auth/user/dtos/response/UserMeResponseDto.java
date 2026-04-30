package com.example.demo.domain.auth.user.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserMeResponseDto {

    private String email;
    private String name;
    private String phone;
    private LocalDate birth;
    private boolean hasLocalPassword;
    private boolean hasKakao;
    private boolean hasNaver;
    private String userStatus;
    private Integer unitNo;
}
