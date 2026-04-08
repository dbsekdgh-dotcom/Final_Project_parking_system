package com.example.demo.domain.user.auth.controller;

import com.example.demo.global.util.admin.AdminJWTUtil;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class UserAuthTestController {

    private final AdminJWTUtil adminJWTUtil;

    @GetMapping("/token")
    public Map<String, String> generateTestToken() {
        Map<String, Object> claims = Map.of("email", "test@example.com", "role", "ROLE_USER");
        String token = adminJWTUtil.generateUserToken(claims, 60); // 60분짜리 토큰
        return Map.of("token", token);
    }

    @GetMapping("/verify")
    public Claims verifyTestToken(@RequestParam("token") String token) {
        return adminJWTUtil.validateUserToken(token);
    }
}
