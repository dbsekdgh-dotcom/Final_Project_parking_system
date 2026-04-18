package com.example.demo.domain.auth.user.service;

import com.example.demo.domain.resident.User;
import com.example.demo.domain.resident.enums.Status;
import com.example.demo.domain.auth.user.constants.UserAuthConstants;
import com.example.demo.domain.auth.user.dtos.request.UserWithdrawRequestDto;
import com.example.demo.domain.auth.user.repository.SocialAccountRepository;
import com.example.demo.domain.auth.user.repository.UserAuthRepository;
import com.example.demo.global.exception.AuthException;
import com.example.demo.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserWithdrawService {

    private final UserAuthRepository userAuthRepository;
    private final SocialAccountRepository socialAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserVerificationService userVerificationService;

    /**
     * 회원 탈퇴 로직
     * 1. 이메일 기반 유저 조회
     * 2. 비밀번호 및 확인 문구 검증
     * 3. 엔티티 내부 withdraw() 호출 (상태변경 + 폰번호 비식별화)
     * 4. 소셜 연동 정보 삭제
     */
    @Transactional
    public void withdraw(String email, UserWithdrawRequestDto userWithdrawRequestDto) {

        // 1. 유저 조회
        User user = userAuthRepository.findByEmail(email)
                .orElseThrow(() -> new AuthException(ErrorCode.USER_NOT_FOUND));

        log.info("회원 탈퇴 처리 시작 - 이메일: {}", email);

        // 2. 이미 탈퇴한 계정인지 체크
        if (user.getStatus() == Status.DELETED) {
            throw new AuthException(ErrorCode.WITHDRAWN_ACCOUNT);
        }

        // 3. 탈퇴 확인 문구 검증 ("회원 탈퇴" 등)
        if (!UserAuthConstants.WITHDRAW_CONFIRM_TEXT.equals(userWithdrawRequestDto.getConfirmText())) {
            throw new AuthException(ErrorCode.INVALID_CONFIRM_TEXT);
        }

        // 4. 입력 비밀번호 일치 확인 (request 내부 검증)
        if (!userWithdrawRequestDto.getPassword().equals(userWithdrawRequestDto.getConfirmPassword())) {
            throw new AuthException(ErrorCode.PASSWORD_MISMATCH);
        }

        // 5. DB 비밀번호와 대조
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            // 소셜 가입자인데 로컬 비번 없이 탈퇴 시도 시 예외 처리 (필요 시)
            throw new AuthException(ErrorCode.LOGIN_FAILED);
        }

        if (!passwordEncoder.matches(userWithdrawRequestDto.getPassword(), user.getPassword())) {
            throw new AuthException(ErrorCode.WITHDRAW_PASSWORD_MISMATCH);
        }

        // 6. 유저 엔티티 상태 변경 (Status.DELETED, 폰번호 변경, 삭제시간 기록)
        // 엔티티 내부의 withdraw() 메서드가 실행됩니다.
        user.withdraw();

        // 7. 상태 변경을 DB에 즉시 반영 (Update 쿼리 실행)
        // Flush를 해야 이후 소셜 계정 삭제(DELETE 쿼리)와 순서가 꼬이지 않습니다.
        userAuthRepository.saveAndFlush(user);

        // 8. 소셜 계정 연동 정보 삭제 (물리 삭제)
        try {
            socialAccountRepository.deleteByUserId(user.getUserId());
        } catch (Exception e) {
            log.error("소셜 계정 정보 삭제 중 오류 발생 (ID: {}): ", user.getUserId(), e);
            // 소셜 정보 삭제 실패 시에도 유저 탈퇴는 유지할지, 전체 롤백할지 결정 필요
            // 현재는 @Transactional에 의해 전체 롤백됩니다.
            throw new AuthException(ErrorCode.SOCIAL_LINK_FAILED);
        }

        userVerificationService.deleteRefreshToken(email);

        log.info("회원 탈퇴 성공 - 이메일: {}, 변경된 번호: {}", user.getEmail(), user.getPhone());
    }
}