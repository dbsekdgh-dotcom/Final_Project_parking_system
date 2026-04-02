package com.example.demo.domain.user.auth.service;

import com.example.demo.domain.shared.user.User;
import com.example.demo.domain.shared.user.enums.Status;
import com.example.demo.domain.user.auth.dtos.request.UserSignupRequestDto;
import com.example.demo.domain.user.auth.repository.UserAuthRepository;
import com.example.demo.global.exception.AuthException; // 추가
import com.example.demo.global.exception.ErrorCode;     // 추가
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserSignupService {

    private final UserAuthRepository userAuthRepository;
    private final PasswordEncoder passwordEncoder;

    public void signup(UserSignupRequestDto userSignupRequestDto) {

        // 1. 필수 입력 항목 검증 (INVALID_REQUEST)
        // 입력값이 하나라도 비어있으면 클라이언트 잘못이므로 BAD_REQUEST를 보냅니다.
        if (isInvalidRequest(userSignupRequestDto)) {
            throw new AuthException(ErrorCode.INVALID_REQUEST);
        }

        // 2. 이메일 중복 체크 (EMAIL_DUPLICATE)
        if (userAuthRepository.existsByEmail(userSignupRequestDto.getEmail())) {
            throw new AuthException(ErrorCode.EMAIL_DUPLICATE);
        }

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

        } catch (DataIntegrityViolationException e) {
            // 데이터 무결성 오류 (예: DB 제약조건 위반 등)
            throw new AuthException(ErrorCode.INVALID_REQUEST);
        } catch (QueryTimeoutException e) {
            throw new RuntimeException("데이터베이스 응답 시간이 초과되었습니다.");
        } catch (Exception e) {
            throw new RuntimeException("알 수 없는 서버 오류가 발생했습니다.");
        }
    }


    private boolean isInvalidRequest(UserSignupRequestDto dto) {
        return dto.getEmail() == null || dto.getEmail().isBlank() ||
                dto.getPassword() == null || dto.getPassword().length() < 8 ||
                dto.getName() == null || dto.getName().isBlank() ||
                dto.getPhone() == null || dto.getPhone().isBlank() ||
                dto.getBirth() == null;
    }
}