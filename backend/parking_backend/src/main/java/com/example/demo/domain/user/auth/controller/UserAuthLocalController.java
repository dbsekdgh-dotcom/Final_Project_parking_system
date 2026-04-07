package com.example.demo.domain.user.auth.controller;

// 1. ⭐ 이 임포트가 핵심입니다. PrincipalDetails 안의 User가 이 User임을 알려줍니다.
import com.example.demo.domain.shared.user.User;

import com.example.demo.domain.user.auth.dtos.request.*;
import com.example.demo.domain.user.auth.dtos.response.UserFindEmailResponseDto;
import com.example.demo.domain.user.auth.dtos.response.UserLoginResponseDto;
import com.example.demo.domain.user.auth.dtos.response.UserMeResponseDto;
import com.example.demo.domain.user.auth.principal.PrincipalDetails;
import com.example.demo.domain.user.auth.repository.UserAuthRepository;
import com.example.demo.domain.user.auth.service.*;
import com.example.demo.global.exception.AuthException;
import com.example.demo.global.exception.ErrorCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user/auth/local")
@RequiredArgsConstructor
public class UserAuthLocalController {

    private final UserSignupService userSignupService;
    private final UserLoginService userLoginService;
    private final UserLinkService userLinkService;
    private final UserFindService userFindService;
    private final UserAuthRepository userAuthRepository;
    private final UserEmailService userEmailService;
    private final UserVerificationService userVerificationService;


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
    @PostMapping("/find-email")
    public ResponseEntity<UserFindEmailResponseDto> findEmail(@RequestBody UserFindEmailRequestDto userFindEmailRequestDto) {

        String maskedEmail = userFindService.findEmail(userFindEmailRequestDto.getName(), userFindEmailRequestDto.getPhone());

        return ResponseEntity.ok(new UserFindEmailResponseDto(maskedEmail));
    }
    @PostMapping("/send-code")
    public ResponseEntity<?> sendCode(@Valid @RequestBody UserPasswordVerifyRequestDto userPasswordVerifyRequestDto) {

        userAuthRepository.findByNameAndEmailAndPhone(
                userPasswordVerifyRequestDto.getName(),
                userPasswordVerifyRequestDto.getEmail(),
                userPasswordVerifyRequestDto.getPhone()
        ).orElseThrow(()-> new AuthException(ErrorCode.USER_INFORMATION_MISMATCH));

        userEmailService.sendVerificationEmail(userPasswordVerifyRequestDto.getEmail());

        return ResponseEntity.ok(Map.of("message","인증번호가 이메일로 발송되었습니다"));
    }
    @PostMapping("/verify-code")
    public ResponseEntity<?> verifyCode(@Valid @RequestBody UserCodeCheckRequestDto userCodeCheckRequestDto) {

        boolean isVerified = userVerificationService.verifyCode(
                userCodeCheckRequestDto.getEmail(),
                userCodeCheckRequestDto.getCode()
        );

        if(isVerified) {
            userVerificationService.deleteCode(userCodeCheckRequestDto.getEmail());

            userVerificationService.saveVerificationPass(userCodeCheckRequestDto.getEmail());
            return ResponseEntity.ok(Map.of("message","인증에 성공하였습니다."));
        }else {
            throw new AuthException(ErrorCode.VERIFICATION_CODE_MISMATCH);        }
    }


    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody UserPasswordResetRequestDto userPasswordResetRequestDto) {
        if(!userPasswordResetRequestDto.getNewPassword().equals(userPasswordResetRequestDto.getConfirmPassword())) {
            throw new AuthException(ErrorCode.PASSWORD_MISMATCH);
        }

        userLinkService.resetPassword(userPasswordResetRequestDto);

        return ResponseEntity.ok(Map.of("message","비밀번호가 성공적으로 변경되었습니다."));
    }

}