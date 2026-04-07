package com.example.demo.domain.user.auth.controller;

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
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
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
    private final UserWithdrawService userWithdrawService;

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
        ).orElseThrow(() -> new AuthException(ErrorCode.USER_INFORMATION_MISMATCH));

        userEmailService.sendVerificationEmail(userPasswordVerifyRequestDto.getEmail());
        return ResponseEntity.ok(Map.of("message", "인증번호가 이메일로 발송되었습니다"));
    }

    @PostMapping("/verify-code")
    public ResponseEntity<?> verifyCode(@Valid @RequestBody UserCodeCheckRequestDto userCodeCheckRequestDto) {
        boolean isVerified = userVerificationService.verifyCode(
                userCodeCheckRequestDto.getEmail(),
                userCodeCheckRequestDto.getCode()
        );

        if (isVerified) {
            userVerificationService.deleteCode(userCodeCheckRequestDto.getEmail());
            userVerificationService.saveVerificationPass(userCodeCheckRequestDto.getEmail());
            return ResponseEntity.ok(Map.of("message", "인증에 성공하였습니다."));
        } else {
            throw new AuthException(ErrorCode.VERIFICATION_CODE_MISMATCH);
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody UserPasswordResetRequestDto userPasswordResetRequestDto) {
        if (!userPasswordResetRequestDto.getNewPassword().equals(userPasswordResetRequestDto.getConfirmPassword())) {
            throw new AuthException(ErrorCode.PASSWORD_MISMATCH);
        }

        userLinkService.resetPassword(userPasswordResetRequestDto);
        return ResponseEntity.ok(Map.of("message", "비밀번호가 성공적으로 변경되었습니다."));
    }

    /**
     * 회원 탈퇴 (Soft Delete)
     * PrincipalDetails의 이메일을 사용하여 서비스를 호출합니다.
     */
    @DeleteMapping("/withdraw")
    public ResponseEntity<String> withdraw(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @Valid @RequestBody UserWithdrawRequestDto userWithdrawRequestDto
    ) {
        // 필터 로직 상 userId가 null일 수 있으므로, 확실한 식별자인 email을 사용합니다.
        String email = principalDetails.getUser().getEmail();

        log.info("회원 탈퇴 요청 API 호출 - 유저 이메일: {}", email);

        // 서비스 레이어의 withdraw(String email, ...)를 호출합니다.
        userWithdrawService.withdraw(email, userWithdrawRequestDto);

        return ResponseEntity.ok("회원 탈퇴 처리가 완료되었습니다.");
    }
}