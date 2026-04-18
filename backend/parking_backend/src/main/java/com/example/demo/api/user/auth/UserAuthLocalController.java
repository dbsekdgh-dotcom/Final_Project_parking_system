package com.example.demo.api.user.auth;

import com.example.demo.domain.auth.user.dtos.request.*;
import com.example.demo.domain.auth.user.dtos.response.UserFindResponseDto;
import com.example.demo.domain.auth.user.dtos.response.UserLoginResponseDto;
import com.example.demo.domain.auth.user.dtos.response.UserMeResponseDto;
import com.example.demo.domain.auth.user.principal.PrincipalDetails;
import com.example.demo.domain.auth.user.repository.UserAuthRepository;
import com.example.demo.domain.auth.user.service.*;
import com.example.demo.global.exception.AuthException;
import com.example.demo.global.exception.ErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "1. 로컬 계정 관리 (Local Auth)", description = "이메일 회원가입, 로그인 및 계정 관리 API")
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

    @Operation(summary = "회원가입", description = "새로운 사용자를 등록합니다. (토큰 불필요)")
    @PostMapping("/signup")
    public ResponseEntity<String> signup(@Valid @RequestBody UserSignupRequestDto userSignupRequestDto) {
        userSignupService.signup(userSignupRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body("회원가입이 성공적으로 완료되었습니다.");
    }

    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인합니다. 성공 시 JWT가 쿠키로 발급됩니다. (토큰 불필요)")
    @PostMapping("/login")
    public ResponseEntity<UserLoginResponseDto> userLogin(
            @Valid @RequestBody UserLoginRequestDto userLoginRequestDto,
            HttpServletResponse response) {
        UserLoginResponseDto userLoginResponseDto = userLoginService.userLogin(userLoginRequestDto, response);
        return ResponseEntity.ok(userLoginResponseDto);
    }

    @Operation(summary = "비밀번호 연동", description = "소셜 계정 등에 로컬 비밀번호를 추가로 연동합니다.", security = @SecurityRequirement(name = "jwtAuth"))
    @PostMapping("/link-password")
    public ResponseEntity<String> linkPassword(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @Valid @RequestBody UserPasswordUpdateRequestDto userPasswordUpdateRequestDto) {
        String email = principalDetails.getUser().getEmail();
        userLinkService.addLocalPassword(email, userPasswordUpdateRequestDto);
        return ResponseEntity.ok("연동 성공!");
    }

    @Operation(summary = "내 정보 조회", description = "현재 로그인한 사용자의 연동 상태 및 정보를 가져옵니다.", security = @SecurityRequirement(name = "jwtAuth"))
    @GetMapping("/me")
    public ResponseEntity<UserMeResponseDto> getMyInfo(@AuthenticationPrincipal PrincipalDetails principalDetails) {
        String email = principalDetails.getUser().getEmail();
        UserMeResponseDto userMeResponseDto = userLinkService.getMyLinkStatus(email);
        return ResponseEntity.ok(userMeResponseDto);
    }

    @Operation(summary = "이메일 찾기", description = "이름과 전화번호를 통해 가입된 이메일을 찾습니다. (토큰 불필요)")
    @PostMapping("/find-email")
    public ResponseEntity<UserFindResponseDto> findEmail(@Valid @RequestBody UserFindEmailRequestDto userFindEmailRequestDto) {
        UserFindResponseDto response = userFindService.findEmail(userFindEmailRequestDto.getName(), userFindEmailRequestDto.getPhone());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "비밀번호 재설정 인증코드 발송", description = "비밀번호를 잊었을 때, 인증번호를 이메일로 발송합니다. (토큰 불필요)")
    @PostMapping("/send-code")
    public ResponseEntity<?> sendCode(@Valid @RequestBody UserPasswordVerifyRequestDto userPasswordVerifyRequestDto) {
        // ... (로직 동일)
        userEmailService.sendVerificationEmail(userPasswordVerifyRequestDto.getEmail());
        return ResponseEntity.ok(Map.of("message", "인증번호가 이메일로 발송되었습니다"));
    }

    @Operation(summary = "인증코드 검증", description = "발송된 이메일 인증코드가 맞는지 확인합니다. (토큰 불필요)")
    @PostMapping("/verify-code")
    public ResponseEntity<?> verifyCode(@Valid @RequestBody UserCodeCheckRequestDto userCodeCheckRequestDto) {
        userVerificationService.verifyCode(userCodeCheckRequestDto.getEmail(), userCodeCheckRequestDto.getCode());
        userVerificationService.saveVerificationPass(userCodeCheckRequestDto.getEmail());
        return ResponseEntity.ok(Map.of("message", "인증에 성공하였습니다."));
    }

    @Operation(summary = "비밀번호 재설정", description = "인증 완료 후 새로운 비밀번호로 변경합니다. (토큰 불필요)")
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody UserPasswordResetRequestDto userPasswordResetRequestDto) {
        // ... (로직 동일)
        userLinkService.resetPassword(userPasswordResetRequestDto);
        return ResponseEntity.ok(Map.of("message", "비밀번호가 성공적으로 변경되었습니다."));
    }

    @Operation(summary = "회원 탈퇴", description = "사용자 계정을 삭제합니다.", security = @SecurityRequirement(name = "jwtAuth"))
    @DeleteMapping("/withdraw")
    public ResponseEntity<String> withdraw(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @Valid @RequestBody UserWithdrawRequestDto userWithdrawRequestDto) {
        String email = principalDetails.getUser().getEmail();
        userWithdrawService.withdraw(email, userWithdrawRequestDto);
        return ResponseEntity.ok("회원 탈퇴가 성공적으로 처리되었습니다.");
    }

    @Operation(summary = "계정 복구 코드 발송", description = "탈퇴/비활성 계정 복구를 위한 인증번호를 발송합니다. (토큰 불필요)")
    @PostMapping("/send-recover-code")
    public ResponseEntity<?> sendRecoverCode(@Valid @RequestBody UserRecoverSendCodeRequestDto userRecoverSendCodeRequestDto) {
        userFindService.sendRecoverCode(userRecoverSendCodeRequestDto);
        return ResponseEntity.ok(Map.of("message", "계정 복구 인증번호가 이메일로 발송되었습니다."));
    }

    @Operation(summary = "계정 복구 완료", description = "인증번호 확인 후 계정을 다시 활성화합니다. (토큰 불필요)")
    @PostMapping("/recover")
    public ResponseEntity<Map<String, String>> recoverAccount(@Valid @RequestBody UserRecoverRequestDto userRecoverRequestDto) {
        userLocalRecoverService.recoverAccount(userRecoverRequestDto);
        return ResponseEntity.ok(Map.of("message", "계정이 성공적으로 복구되었습니다. 다시 로그인해 주세요."));
    }

    @Operation(summary = "로그아웃", description = "로그아웃 처리를 하고 쿠키를 삭제합니다.", security = @SecurityRequirement(name = "jwtAuth"))
    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            HttpServletResponse response) {
        if (principalDetails != null) {
            userLoginService.logout(principalDetails.getUser().getEmail(), response);
        }
        return ResponseEntity.ok(Map.of("message", "로그아웃이 성공적으로 처리되었습니다."));
    }

    @Operation(summary = "이메일 중복 체크", description = "사용 가능한 이메일인지 실시간으로 확인합니다. (토큰 불필요)")
    @PostMapping("/check-email")
    public ResponseEntity<Map<String, String>> checkEmail(@Valid @RequestBody UserEmailCheckRequestDto userEmailCheckRequestDto) {
        userSignupService.checkEmailAvailability(userEmailCheckRequestDto);
        return ResponseEntity.ok(Map.of("message", "사용 가능한 이메일입니다."));
    }
}