package com.example.demo.global.security.admin;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

//관리자 인증용Dto
public class AdminAuthDto extends User {
    private String loginId;
    private String name;

    public AdminAuthDto(String loginId, String password, String name){
        // 1. super에는 (아이디, 비밀번호, 권한리스트) 딱 3개만 순서대로 넘김. 순서 꼭 지켜야함
        super(loginId, password, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        // 2. 부모가 모르는 정보(loginId, name)는 내 필드에 따로 저장
        this.loginId = loginId;
        this.name = name;
    }

    public String getName(){
        return name;
    }

    public Map<String,Object> getClaims(){
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("loginId",loginId);
        dataMap.put("name",name);
        dataMap.put("role","ROLE_ADMIN"); // 관리자 권한 고정
        return dataMap;
    }
}
