package com.example.demo.domain.auth.user.enums;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Provider {

    LOCAL("일반 로그인"),
    KAKAO("카카오 로그인"),
    NAVER("네이버 로그인");
    private final String description;
}
