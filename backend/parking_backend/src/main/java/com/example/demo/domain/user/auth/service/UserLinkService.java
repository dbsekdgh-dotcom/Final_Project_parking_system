package com.example.demo.domain.user.auth.service;

import com.example.demo.domain.shared.user.User;
import com.example.demo.domain.shared.user.UserRepository;
import com.example.demo.domain.shared.user.enums.Status;
import com.example.demo.domain.user.auth.dtos.request.UserPasswordResetRequestDto;
import com.example.demo.domain.user.auth.dtos.request.UserPasswordUpdateRequestDto;
import com.example.demo.domain.user.auth.dtos.response.UserMeResponseDto;
import com.example.demo.domain.user.auth.repository.SocialAccountRepository;
import com.example.demo.domain.user.entity.SocialAccount;
import com.example.demo.domain.user.enums.Provider;
import com.example.demo.global.exception.AuthException;
import com.example.demo.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserLinkService {

    private final SocialAccountRepository socialAccountRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserVerificationService userVerificationService;

    @Transactional
    public void addLocalPassword(String email, UserPasswordUpdateRequestDto userPasswordUpdateRequestDto) {
        User user = userRepository.findByEmail(email)
                .filter(u -> u.getStatus() == Status.ACTIVE)
                .orElseThrow(()-> new AuthException(ErrorCode.USER_NOT_FOUND));

        if (!userPasswordUpdateRequestDto.getPassword().equals(userPasswordUpdateRequestDto.getPasswordConfirm())) {
            throw new AuthException(ErrorCode.PASSWORD_MISMATCH);
        }

        if( user.getPassword() != null) {
            throw new AuthException(ErrorCode.ALREADY_LINKED_LOCAL);
        }

        String encodedPassword = passwordEncoder.encode(userPasswordUpdateRequestDto.getPassword());
        user.addLocalPassword(encodedPassword);

    }
    @Transactional(readOnly = true)
    public UserMeResponseDto getMyLinkStatus(String email) {

        User user = userRepository.findByEmail(email)
                .filter(u -> u.getStatus() == Status.ACTIVE)
                .orElseThrow(()-> new AuthException(ErrorCode.USER_NOT_FOUND));

        List<SocialAccount> socialAccounts = socialAccountRepository.findByUser(user);

        boolean hasKakao = socialAccounts.stream()
                .anyMatch(sa -> sa.getProvider() == Provider.KAKAO);
        boolean hasNaver = socialAccounts.stream()
                .anyMatch(sa -> sa.getProvider() == Provider.NAVER);

        return UserMeResponseDto.builder()
                .email(user.getEmail())
                .name(user.getName())
                .phone(user.getPhone())
                .birth(user.getBirth())
                .hasLocalPassword(user.getPassword() != null)
                .hasKakao(hasKakao)
                .hasNaver(hasNaver)
                .build();
    }
    @Transactional
    public void resetPassword(UserPasswordResetRequestDto userPasswordResetRequestDto) {

        if(!userVerificationService.hasVerificationPass(userPasswordResetRequestDto.getEmail())){
            throw new AuthException(ErrorCode.UNAUTHORIZED_ACCESS);
        }


        User user = userRepository.findByEmail(userPasswordResetRequestDto.getEmail())
                .filter(u -> u.getStatus() == Status.ACTIVE)
                .orElseThrow(()-> new AuthException(ErrorCode.USER_NOT_FOUND));

        String encodedPassword = passwordEncoder.encode(userPasswordResetRequestDto.getNewPassword());

        user.addLocalPassword(encodedPassword);

        userVerificationService.deleteVerificationPass(userPasswordResetRequestDto.getEmail());


    }
}
