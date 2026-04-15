package com.example.demo.domain.user.auth.controller;

import com.example.demo.domain.user.auth.dtos.request.*;
import com.example.demo.domain.user.auth.dtos.response.UserFindResponseDto;
import com.example.demo.domain.user.auth.dtos.response.UserLoginResponseDto;
import com.example.demo.domain.user.auth.dtos.response.UserMeResponseDto;
import com.example.demo.domain.user.auth.principal.PrincipalDetails;
import com.example.demo.domain.user.auth.repository.UserAuthRepository;
import com.example.demo.domain.user.auth.service.*;
import com.example.demo.global.exception.AuthException;
import com.example.demo.global.exception.ErrorCode;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.parameters.P;
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
    private final UserLocalRecoverService userLocalRecoverService;

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@Valid @RequestBody UserSignupRequestDto userSignupRequestDto) {
        userSignupService.signup(userSignupRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body("회원가입이 성공적으로 완료되었습니다.");
    }

    /**
     * 로그인 (수정됨)
     * HttpServletResponse를 파라미터로 받아 서비스에 넘겨줍니다.
     */
    @PostMapping("/login")
    public ResponseEntity<UserLoginResponseDto> userLogin(
            @Valid @RequestBody UserLoginRequestDto userLoginRequestDto,
            HttpServletResponse response) { // ⭐ response 추가

        // 이제 서비스에서 쿠키를 구워서 response에 담아줍니다.
        UserLoginResponseDto userLoginResponseDto = userLoginService.userLogin(userLoginRequestDto, response);
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
    public ResponseEntity<UserMeResponseDto> getMyInfo(@AuthenticationPrincipal PrincipalDetails principalDetails) {
        String email = principalDetails.getUser().getEmail();
        UserMeResponseDto userMeResponseDto = userLinkService.getMyLinkStatus(email);
        return ResponseEntity.ok(userMeResponseDto);
    }

    @PostMapping("/find-email")
    public ResponseEntity<UserFindResponseDto> findEmail(@Valid @RequestBody UserFindEmailRequestDto userFindEmailRequestDto) {
        UserFindResponseDto response = userFindService.findEmail(userFindEmailRequestDto.getName(), userFindEmailRequestDto.getPhone());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/send-code")
    public ResponseEntity<?> sendCode(@Valid @RequestBody UserPasswordVerifyRequestDto userPasswordVerifyRequestDto) {
        userAuthRepository.findByNameAndEmailAndPhoneForReset(
                userPasswordVerifyRequestDto.getName(),
                userPasswordVerifyRequestDto.getEmail(),
                userPasswordVerifyRequestDto.getPhone()
        ).orElseThrow(() -> new AuthException(ErrorCode.USER_INFORMATION_MISMATCH));

        userEmailService.sendVerificationEmail(userPasswordVerifyRequestDto.getEmail());
        return ResponseEntity.ok(Map.of("message", "인증번호가 이메일로 발송되었습니다"));
    }

    /**
     * 이메일 인증 코드 검증 (수정됨)
     */
    @PostMapping("/verify-code")
    public ResponseEntity<?> verifyCode(@Valid @RequestBody UserCodeCheckRequestDto userCodeCheckRequestDto) {
        // 서비스 내부에서 에러(만료/불일치)를 직접 던지므로 별도의 if문이 필요 없음
        userVerificationService.verifyCode(
                userCodeCheckRequestDto.getEmail(),
                userCodeCheckRequestDto.getCode()
        );

        // 여기까지 코드가 진행되었다면 인증 성공임
        userVerificationService.saveVerificationPass(userCodeCheckRequestDto.getEmail());
        return ResponseEntity.ok(Map.of("message", "인증에 성공하였습니다."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody UserPasswordResetRequestDto userPasswordResetRequestDto) {
        if (!userPasswordResetRequestDto.getNewPassword().equals(userPasswordResetRequestDto.getConfirmPassword())) {
            throw new AuthException(ErrorCode.PASSWORD_MISMATCH);
        }
        userLinkService.resetPassword(userPasswordResetRequestDto);
        return ResponseEntity.ok(Map.of("message", "비밀번호가 성공적으로 변경되었습니다."));
    }

    @DeleteMapping("/withdraw")
    public ResponseEntity<String> withdraw(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @Valid @RequestBody UserWithdrawRequestDto userWithdrawRequestDto) {
        String email = principalDetails.getUser().getEmail();
        userWithdrawService.withdraw(email, userWithdrawRequestDto);
        return ResponseEntity.ok("회원 탈퇴가 성공적으로 처리되었습니다.");
    }

    @PostMapping("/send-recover-code")
    public ResponseEntity<?> sendRecoverCode(@Valid @RequestBody UserRecoverSendCodeRequestDto userRecoverSendCodeRequestDto) {
        userFindService.sendRecoverCode(userRecoverSendCodeRequestDto);
        return ResponseEntity.ok(Map.of("message", "계정 복구 인증번호가 이메일로 발송되었습니다."));
    }

    @PostMapping("/recover")
    public ResponseEntity<Map<String, String>> recoverAccount(@Valid @RequestBody UserRecoverRequestDto userRecoverRequestDto) {
        userLocalRecoverService.recoverAccount(userRecoverRequestDto);
        return ResponseEntity.ok(Map.of("message", "계정이 성공적으로 복구되었습니다. 다시 로그인해 주세요."));
    }

    /**
     * 로그아웃 (수정됨)
     * 쿠키 삭제를 위해 HttpServletResponse를 서비스에 전달합니다.
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            HttpServletResponse response) { // ⭐ response 추가

        if (principalDetails != null) {
            String email = principalDetails.getUser().getEmail();
            // Redis 삭제 + 브라우저 쿠키 삭제 명령 진행
            userLoginService.logout(email, response);
        }
        return ResponseEntity.ok(Map.of("message", "로그아웃이 성공적으로 처리되었습니다."));
    }
    @PostMapping("/check-email")
    public ResponseEntity<Map<String, String>> checkEmail(@Valid @RequestBody UserEmailCheckRequestDto userEmailCheckRequestDto) {
        log.info("이메일 중복 체크 요청: {}", userEmailCheckRequestDto.getEmail());

        userSignupService.checkEmailAvailability(userEmailCheckRequestDto);

        return ResponseEntity.ok(Map.of("message", "사용 가능한 이메일입니다."));
    }


}