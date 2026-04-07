package com.example.demo.domain.user.auth.service;

import com.example.demo.domain.shared.user.User;
import com.example.demo.domain.shared.user.enums.Status;
import com.example.demo.domain.user.auth.constants.UserAuthConstants;
import com.example.demo.domain.user.auth.dtos.request.UserWithdrawRequestDto;
import com.example.demo.domain.user.auth.repository.SocialAccountRepository;
import com.example.demo.domain.user.auth.repository.UserAuthRepository;
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

    /**
     * 회원 탈퇴 로직
     * 1. 확실한 이메일 기반 유저 조회 (ID null 이슈 방지)
     * 2. 상태값 변경(DELETED)을 소셜 정보 삭제보다 먼저 DB에 반영(Flush)하여 업데이트 유실 방지
     */
    @Transactional
    public void withdraw(String email, UserWithdrawRequestDto userWithdrawRequestDto) {

        // 1. 이메일로 유저 조회 (DB에서 직접 가져와 영속성 컨텍스트에 로드)
        User user = userAuthRepository.findByEmail(email)
                .orElseThrow(() -> new AuthException(ErrorCode.USER_NOT_FOUND));

        log.info("회원 탈퇴 처리 시작 - 이메일: {}, 추출된 ID: {}", email, user.getUserId());

        // 2. 이미 탈퇴한 계정인지 체크 (중복 요청 방지)
        if (user.getStatus() == Status.DELETED) {
            throw new AuthException(ErrorCode.WITHDRAWN_ACCOUNT);
        }

        // 3. 탈퇴 확인 문구 검증 (예: "회원 탈퇴")
        if (!UserAuthConstants.WITHDRAW_CONFIRM_TEXT.equals(userWithdrawRequestDto.getConfirmText())) {
            throw new AuthException(ErrorCode.INVALID_CONFIRM_TEXT);
        }

        // 4. 입력받은 비밀번호와 확인용 비밀번호가 일치하는지 검증
        if (!userWithdrawRequestDto.getPassword().equals(userWithdrawRequestDto.getConfirmPassword())) {
            throw new AuthException(ErrorCode.PASSWORD_MISMATCH);
        }

        // 5. 현재 DB 비밀번호와 대조
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            throw new AuthException(ErrorCode.LOGIN_FAILED);
        }

        if (!passwordEncoder.matches(userWithdrawRequestDto.getPassword(), user.getPassword())) {
            throw new AuthException(ErrorCode.WITHDRAW_PASSWORD_MISMATCH);
        }

        // 6. 유저 엔티티 상태 변경 (Status.DELETED 및 삭제 시간 기록)
        user.withdraw();

        // ⭐ 핵심: 상태 변경을 DB에 즉시 반영 (Update 쿼리 강제 실행)
        // saveAndFlush를 사용해야 뒤에 오는 @Modifying(DELETE) 쿼리에 의해 업데이트가 씹히지 않습니다.
        userAuthRepository.saveAndFlush(user);

        // 7. 소셜 계정 연동 정보 물리 삭제
        // 유저 상태가 성공적으로 변경된 후 소셜 연동 데이터를 지웁니다.
        socialAccountRepository.deleteByUserId(user.getUserId());

        log.info("회원 탈퇴 성공 - ID: {}, 최종 상태: {}", user.getUserId(), user.getStatus());
    }
}