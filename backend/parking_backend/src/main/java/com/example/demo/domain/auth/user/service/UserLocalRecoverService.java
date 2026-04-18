package com.example.demo.domain.auth.user.service;

import com.example.demo.domain.resident.User;
import com.example.demo.domain.resident.enums.Status;
import com.example.demo.domain.auth.user.dtos.request.UserRecoverRequestDto;
import com.example.demo.domain.auth.user.repository.UserAuthRepository;
import com.example.demo.global.exception.AuthException;
import com.example.demo.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserLocalRecoverService {

    private final UserAuthRepository userAuthRepository;
    private final StringRedisTemplate redisTemplate;
    private final PasswordEncoder passwordEncoder;

    /**
     * 최종 계정 복구 처리 (비밀번호 재설정 및 중복 번호 검증 포함)
     */
    @Transactional
    public void recoverAccount(UserRecoverRequestDto userRecoverRequestDto) {

        // 1. [비밀번호 검증] 새 비밀번호와 확인용 비밀번호가 일치하는지 체크
        // DTO 필드명인 passwordConfirm에 맞춰서 비교 로직을 작성했습니다.
        if (userRecoverRequestDto.getNewPassword() == null ||
                !userRecoverRequestDto.getNewPassword().equals(userRecoverRequestDto.getPasswordConfirm())) {
            log.warn("복구 실패 - 비밀번호 확인 불일치: {}", userRecoverRequestDto.getEmail());
            throw new AuthException(ErrorCode.PASSWORD_MISMATCH);
        }

        // 2. [인증번호 검증] Redis에서 인증번호 가져오기 및 검증
        String savedCode = redisTemplate.opsForValue().get("AUTH_CODE:" + userRecoverRequestDto.getEmail());

        if (savedCode == null || !savedCode.equals(userRecoverRequestDto.getAuthCode())) {
            log.warn("복구 실패 - 인증번호 불일치 혹은 만료: {}", userRecoverRequestDto.getEmail());
            throw new AuthException(ErrorCode.INVALID_AUTH_CODE);
        }

        // 3. 탈퇴 상태인 유저 찾기 (이메일 기준)
        User user = userAuthRepository.findByEmailAndStatus(userRecoverRequestDto.getEmail(), Status.DELETED)
                .orElseThrow(() -> new AuthException(ErrorCode.USER_NOT_FOUND));

        // 4. 전화번호 원복 추출 (01012345678_del_... 에서 언더바 앞부분만 추출)
        // 기존에 전화번호를 변조해서 저장했던 방식에 맞춰 split을 수행합니다.
        String rawPhone = user.getPhone().split("_")[0];

        // 5. [핵심 보안] 복구 전 중복 번호 체크
        // 현재 해당 번호를 'ACTIVE' 상태로 사용 중인 새 계정이 이미 있다면 복구를 중단시킵니다.
        if (userAuthRepository.existsByPhoneAndStatus(rawPhone, Status.ACTIVE)) {
            log.warn("복구 불가 - 이미 해당 번호로 활동 중인 계정이 존재함: {}", rawPhone);
            throw new AuthException(ErrorCode.PHONE_ALREADY_ACTIVE);
        }

        // 6. 새 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(userRecoverRequestDto.getNewPassword());

        // 7. 계정 복구 실행 (상태를 ACTIVE로, 번호를 rawPhone으로, 비밀번호를 새 암호로 업데이트)
        // Repository에 정의된 recoverByEmailWithPassword 메서드를 호출합니다.
        userAuthRepository.recoverByEmailWithPassword(
                userRecoverRequestDto.getEmail(),
                rawPhone,
                encodedPassword
        );

        // 8. 복구 성공 후 Redis에 남아있는 인증 코드 삭제 (보안 및 재사용 방지)
        redisTemplate.delete("AUTH_CODE:" + userRecoverRequestDto.getEmail());

        log.info("계정 복구 완료 성공: 이메일={}, 복구된 번호={}", userRecoverRequestDto.getEmail(), rawPhone);
    }
}