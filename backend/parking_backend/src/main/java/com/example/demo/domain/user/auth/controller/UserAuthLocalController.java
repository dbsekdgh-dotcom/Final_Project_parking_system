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
    private final UserRecoverService userRecoverService;

    /**
     * 회원가입
     */
    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody UserSignupRequestDto userSignupRequestDto) {
        userSignupService.signup(userSignupRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body("회원가입이 성공적으로 완료되었습니다.");
    }

    /**
     * 로그인
     */
    @PostMapping("/login")
    public ResponseEntity<UserLoginResponseDto> userLogin(@RequestBody UserLoginRequestDto userLoginRequestDto) {
        UserLoginResponseDto userLoginResponseDto = userLoginService.userLogin(userLoginRequestDto);
        return ResponseEntity.ok(userLoginResponseDto);
    }

    /**
     * 소셜 계정에 로컬 비밀번호 연동
     */
    @PostMapping("/link-password")
    public ResponseEntity<String> linkPassword(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @Valid @RequestBody UserPasswordUpdateRequestDto userPasswordUpdateRequestDto) {

        String email = principalDetails.getUser().getEmail();
        userLinkService.addLocalPassword(email, userPasswordUpdateRequestDto);

        return ResponseEntity.ok("연동 성공!");
    }

    /**
     * 내 연동 정보 조회 (로컬/소셜 여부)
     */
    @GetMapping("/me")
    public ResponseEntity<UserMeResponseDto> getMyInfo(
            @AuthenticationPrincipal PrincipalDetails principalDetails) {

        String email = principalDetails.getUser().getEmail();
        UserMeResponseDto userMeResponseDto = userLinkService.getMyLinkStatus(email);

        return ResponseEntity.ok(userMeResponseDto);
    }

    /**
     * 아이디(이메일) 찾기
     */
    @PostMapping("/find-email")
    public ResponseEntity<UserFindResponseDto> findEmail(@RequestBody UserFindEmailRequestDto userFindEmailRequestDto) {
        UserFindResponseDto response = userFindService.findEmail(
                userFindEmailRequestDto.getName(),
                userFindEmailRequestDto.getPhone()
        );
        return ResponseEntity.ok(response);
    }

    /**
     * 비밀번호 재설정용 인증 코드 발송
     */
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
     * 이메일 인증 코드 검증
     */
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

    /**
     * 비밀번호 재설정 (실제 변경)
     */
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
     */
    @DeleteMapping("/withdraw")
    public ResponseEntity<String> withdraw(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @RequestBody UserWithdrawRequestDto userWithdrawRequestDto) {

        String email = principalDetails.getUser().getEmail();
        userWithdrawService.withdraw(email, userWithdrawRequestDto);

        return ResponseEntity.ok("회원 탈퇴가 성공적으로 처리되었습니다.");
    }

    /**
     * [계정 복구] 인증번호 발송
     * 💡 변경 사항: userEmailService 대신 UserFindService를 호출하여 이름/이메일/전번 검증을 수행합니다.
     */
    @PostMapping("/send-recover-code")
    public ResponseEntity<?> sendRecoverCode(@Valid @RequestBody UserRecoverSendCodeRequestDto userRecoverSendCodeRequestDto) {
        log.info("계정 복구 인증번호 발송 요청 이메일: {}", userRecoverSendCodeRequestDto.getEmail());

        // UserFindService의 sendRecoverCode를 호출하여 DB 검증 후 메일 발송
        userFindService.sendRecoverCode(userRecoverSendCodeRequestDto);

        return ResponseEntity.ok(Map.of("message", "계정 복구 인증번호가 이메일로 발송되었습니다."));
    }

    /**
     * [계정 복구] 최종 승인 및 비밀번호 재설정
     */
    @PostMapping("/recover")
    public ResponseEntity<Map<String, String>> recoverAccount(@Valid @RequestBody UserRecoverRequestDto userRecoverRequestDto) {
        log.info("계정 복구 최종 요청 이메일: {}", userRecoverRequestDto.getEmail());

        userRecoverService.recoverAccount(userRecoverRequestDto);

        return ResponseEntity.ok(Map.of("message", "계정이 성공적으로 복구되었습니다. 다시 로그인해 주세요."));
    }
}