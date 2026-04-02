package com.example.demo.domain.user.auth.service;


import com.example.demo.domain.shared.user.User;
import com.example.demo.domain.shared.user.enums.Status;
import com.example.demo.domain.user.auth.dtos.request.UserSignupRequestDto;
import com.example.demo.domain.user.auth.repository.UserAuthRepository;
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

        if (userSignupRequestDto.getEmail() == null || userSignupRequestDto.getEmail().isBlank()) {
            throw new IllegalArgumentException("이메일은 필수 입력 항목입니다.");
        }

        if (userSignupRequestDto.getPassword() == null || userSignupRequestDto.getPassword().length()<8) {
            throw new IllegalArgumentException("비밀번호는 최소 8자 이상이어야 합니다.");
        }
        if (userSignupRequestDto.getName() == null || userSignupRequestDto.getName().isBlank()) {
            throw new IllegalArgumentException("이름을 입력해 주세요.");
        }
        if (userSignupRequestDto.getPhone() == null || userSignupRequestDto.getPhone().isBlank()) {
            throw new IllegalArgumentException("전화번호를 입력해 주세요.");
        }
        if (userSignupRequestDto.getBirth() == null) {
            throw new IllegalArgumentException("생년월일을 선택해 주세요.");
        }

        if (userAuthRepository.existsByEmail(userSignupRequestDto.getEmail())) {
            throw new IllegalStateException("이미 사용 중인 이메일입니다.");
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
            throw new RuntimeException("입력 데이터 형식이 올바르지 않습니다.");
        } catch (QueryTimeoutException e) {
            throw new RuntimeException("데이터베이스 응답 시간이 초과되었습니다.");
        } catch (Exception e) {
            throw new RuntimeException("알 수 없는 서버 오류가 발생했습니다.");
        }


    }



}
