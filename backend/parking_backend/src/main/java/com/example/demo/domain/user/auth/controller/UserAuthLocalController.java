package com.example.demo.domain.user.auth.controller;

// 1. ⭐ 이 임포트가 핵심입니다. PrincipalDetails 안의 User가 이 User임을 알려줍니다.
import com.example.demo.domain.shared.user.User;

import com.example.demo.domain.user.auth.dtos.request.UserLoginRequestDto;
import com.example.demo.domain.user.auth.dtos.request.UserPasswordUpdateRequestDto;
import com.example.demo.domain.user.auth.dtos.request.UserSignupRequestDto;
import com.example.demo.domain.user.auth.dtos.response.UserLoginResponseDto;
import com.example.demo.domain.user.auth.dtos.response.UserMeResponseDto;
import com.example.demo.domain.user.auth.principal.PrincipalDetails;
import com.example.demo.domain.user.auth.service.UserLinkService;
import com.example.demo.domain.user.auth.service.UserLoginService;
import com.example.demo.domain.user.auth.service.UserSignupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user/auth/local")
@RequiredArgsConstructor
public class UserAuthLocalController {

    private final UserSignupService userSignupService;
    private final UserLoginService userLoginService;
    private final UserLinkService userLinkService;

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

    @PostMapping("/link-password")
    public ResponseEntity<String> linkPassword(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @Valid @RequestBody UserPasswordUpdateRequestDto userPasswordUpdateRequestDto) {

        // 2. ⭐ 이제 위에서 User를 import 했기 때문에,
        // principalDetails.getUser()가 반환하는 객체가 User 엔티티임을 인식하고
        // 그 안의 getEmail()을 정상적으로 찾아냅니다.
        String email = principalDetails.getUser().getEmail();

        userLinkService.addLocalPassword(email, userPasswordUpdateRequestDto);

        return ResponseEntity.ok("연동 성공!");
    }
    @GetMapping("/me")
    public ResponseEntity<UserMeResponseDto> getMyInfo(
            @AuthenticationPrincipal PrincipalDetails principalDetails) {

        String email = principalDetails.getUser().getEmail();

        UserMeResponseDto userMeResponseDto = userLinkService.getMyLinkStatus(email);

        return ResponseEntity.ok(userMeResponseDto);
    }

}