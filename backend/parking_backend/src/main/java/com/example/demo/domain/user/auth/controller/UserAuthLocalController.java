package com.example.demo.domain.user.auth.controller;

import com.example.demo.domain.user.auth.dtos.request.UserLoginRequestDto;
import com.example.demo.domain.user.auth.dtos.request.UserSignupRequestDto;
import com.example.demo.domain.user.auth.dtos.response.UserLoginResponseDto;
import com.example.demo.domain.user.auth.service.UserLoginService;
import com.example.demo.domain.user.auth.service.UserSignupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user/auth/local")
@RequiredArgsConstructor
public class UserAuthLocalController {

    private final UserSignupService userSignupService;

    private final UserLoginService userLoginService;

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody UserSignupRequestDto userSignupRequestDto) {

        userSignupService.signup(userSignupRequestDto);

        return ResponseEntity.status(HttpStatus.CREATED).body("회원가입이 성공적으로 완료되었습니다.");
    }
    @PostMapping("/login")
    public ResponseEntity<UserLoginResponseDto> userLogin(@RequestBody UserLoginRequestDto userLoginRequestDto) {

        UserLoginResponseDto userLoginResponseDto = userLoginService.userLogin(userLoginRequestDto);

        return ResponseEntity.ok(userLoginResponseDto);
    }


}
