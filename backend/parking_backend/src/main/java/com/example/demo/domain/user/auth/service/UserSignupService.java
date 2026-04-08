package com.example.demo.domain.user.auth.service;

import com.example.demo.domain.shared.user.User;
import com.example.demo.domain.shared.user.enums.Status;
import com.example.demo.domain.user.auth.dtos.request.UserSignupRequestDto;
import com.example.demo.domain.user.auth.repository.UserAuthRepository;
import com.example.demo.global.exception.AuthException;
import com.example.demo.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserSignupService {

    private final UserAuthRepository userAuthRepository;
    private final PasswordEncoder passwordEncoder;

    public void signup(UserSignupRequestDto userSignupRequestDto) {

        // 1. 필수 입력 항목 검증
        if (isInvalidRequest(userSignupRequestDto)) {
            throw new AuthException(ErrorCode.INVALID_REQUEST);
        }

        // 2. 이메일 중복 및 상태 체크 (복구 로직의 핵심)
        Optional<User> existingUser = userAuthRepository.findByEmail(userSignupRequestDto.getEmail());

        if (existingUser.isPresent()) {
            User user = existingUser.get();

            if (user.getStatus() == Status.DELETED) {
                // 탈퇴한 계정이면 복구 안내를 위해 전용 에러 코드를 던짐
                log.info("탈퇴한 계정으로 재가입 시도: {}", user.getEmail());
                throw new AuthException(ErrorCode.WITHDRAWN_ACCOUNT);
            } else {
                // 이미 활동 중인 계정이면 일반 중복 에러
                throw new AuthException(ErrorCode.EMAIL_DUPLICATE);
            }
        }

        // 3. 휴대폰 번호 중복 체크 (Unique 제약 조건 방어)
        // 탈퇴 시 번호를 변경하므로, 여기서 걸리는 번호는 현재 사용 중인 번호임
        if (userAuthRepository.existsByPhone(userSignupRequestDto.getPhone())) {
            throw new AuthException(ErrorCode.PHONE_DUPLICATE); // ErrorCode에 PHONE_DUPLICATE 추가 필요
        }

        // 4. 비밀번호 암호화 및 저장
        String encodedPassword = passwordEncoder.encode(userSignupRequestDto.getPassword());

        try {
            User user = User.builder()
                    .email(userSignupRequestDto.getEmail())
                    .password(encodedPassword)
                    .name(userSignupRequestDto.getName())
                    .phone(userSignupRequestDto.getPhone())
                    .birth(userSignupRequestDto.getBirth())
                    .status(Status.ACTIVE)
                    .build();

            userAuthRepository.save(user);
            log.info("회원가입 완료: {}", user.getEmail());

        } catch (DataIntegrityViolationException e) {
            // DB 레벨에서 한 번 더 Unique 제약 조건을 체크 (동시성 이슈 방지)
            log.error("데이터 무결성 위반 발생: {}", e.getMessage());
            throw new AuthException(ErrorCode.INVALID_REQUEST);
        } catch (Exception e) {
            log.error("회원가입 중 예상치 못한 오류: ", e);
            throw new RuntimeException("알 수 없는 서버 오류가 발생했습니다.");
        }
    }

    /**
     * 입력값 유효성 검증 (기본적인 null 및 길이 체크)
     */
    private boolean isInvalidRequest(UserSignupRequestDto dto) {
        return dto.getEmail() == null || dto.getEmail().isBlank() ||
                dto.getPassword() == null || dto.getPassword().length() < 8 ||
                dto.getName() == null || dto.getName().isBlank() ||
                dto.getPhone() == null || dto.getPhone().isBlank() ||
                dto.getBirth() == null;
    }
}