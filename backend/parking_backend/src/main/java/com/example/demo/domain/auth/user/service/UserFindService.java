package com.example.demo.domain.auth.user.service;

import com.example.demo.domain.resident.User;
import com.example.demo.domain.resident.enums.Status;
import com.example.demo.domain.auth.user.dtos.request.UserRecoverSendCodeRequestDto;
import com.example.demo.domain.auth.user.dtos.response.UserFindResponseDto;
import com.example.demo.domain.auth.user.repository.UserAuthRepository;
import com.example.demo.global.exception.AuthException;
import com.example.demo.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserFindService {

    private final UserAuthRepository userAuthRepository;
    private final UserEmailService userEmailService;

    /**
     * 이름과 전화번호로 이메일(아이디) 찾기
     */
    public UserFindResponseDto findEmail(String name, String phone) {
        // 1. Repository에서 이름과 LIKE 번호로 모든 데이터 조회
        List<User> users = userAuthRepository.findByNameAndPhoneForFindingEmail(name, phone);

        if (users == null || users.isEmpty()) {
            log.warn("아이디 찾기 실패 - 일치하는 정보 없음: 이름={}, 번호={}", name, phone);
            throw new AuthException(ErrorCode.USER_NOT_FOUND);
        }

        // [디버깅 로그] 조회된 모든 유저의 상태를 출력
        users.forEach(u -> log.info("조회된 유저 정보: 이메일={}, 상태={}", u.getEmail(), u.getStatus()));

        // 2. 계정 선택 로직
        User targetUser = users.stream()
                .filter(u -> u.getStatus() != null && u.getStatus().name().equals("ACTIVE"))
                .findFirst()
                .orElse(users.get(0));

        // 3. 탈퇴 여부 판단 (DELETED 상태인지 확인)
        boolean isWithdrawn = targetUser.getStatus() != null && targetUser.getStatus().name().equals("DELETED");

        log.info("최종 선택된 타겟 유저: 이메일={}, 탈퇴여부(isWithdrawn)={}", targetUser.getEmail(), isWithdrawn);

        // 4. 결과 DTO 반환
        return UserFindResponseDto.builder()
                .email(maskEmail(targetUser.getEmail()))
                .isWithdrawn(isWithdrawn)
                .build();
    }

    /**
     * 이메일 마스킹 처리 (ex: abc****@naver.com)
     */
    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return email;
        }
        try {
            String[] parts = email.split("@");
            String id = parts[0];
            String domain = parts[1];

            int visibleLen = Math.min(id.length(), 3);
            return id.substring(0, visibleLen) + "****@" + domain;
        } catch (Exception e) {
            return email;
        }
    }

    /**
     * 1단계: 복구용 인증번호 발송 전 유저 검증 및 발송
     * 파라미터 타입을 UserRecoverSendCodeRequestDto로 변경하여 이름, 이메일, 전번을 모두 검증합니다.
     */
    public void sendRecoverCode(UserRecoverSendCodeRequestDto userRecoverSendCodeRequestDto) {
        // 1. 이름, 이메일, 전화번호가 모두 일치하는 '탈퇴 유저'가 있는지 확인 (보안 강화)
        User user = userAuthRepository.findDeletedUserForRecovery(
                userRecoverSendCodeRequestDto.getName(),
                userRecoverSendCodeRequestDto.getEmail(),
                userRecoverSendCodeRequestDto.getPhone()
        ).orElseThrow(() -> new AuthException(ErrorCode.USER_INFORMATION_MISMATCH));

        // 2. 검증 통과 시에만 실제 메일 발송
        userEmailService.sendVerificationEmail(user.getEmail());

        log.info("계정 정보 일치 확인 완료 - 복구 인증번호 발송: {}", user.getEmail());
    }
}