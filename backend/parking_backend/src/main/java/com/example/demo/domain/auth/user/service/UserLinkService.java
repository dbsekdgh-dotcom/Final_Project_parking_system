package com.example.demo.domain.auth.user.service;

import com.example.demo.domain.approval.enums.ApprovalStatus;
import com.example.demo.domain.approval.enums.ApprovalType;
import com.example.demo.domain.approval.repository.ApprovalRepository;
import com.example.demo.domain.resident.User;
import com.example.demo.domain.resident.UserRepository;
import com.example.demo.domain.resident.enums.Status;
import com.example.demo.domain.auth.user.dtos.request.UserPasswordResetRequestDto;
import com.example.demo.domain.auth.user.dtos.request.UserPasswordUpdateRequestDto;
import com.example.demo.domain.auth.user.dtos.response.UserMeResponseDto;
import com.example.demo.domain.auth.user.repository.SocialAccountRepository;
import com.example.demo.domain.auth.user.entity.SocialAccount;
import com.example.demo.domain.auth.user.enums.Provider;
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
    private final ApprovalRepository approvalRepository;

    @Transactional
    public void addLocalPassword(String email, UserPasswordUpdateRequestDto userPasswordUpdateRequestDto) {
        User user = userRepository.findByEmail(email)
                .filter(u -> u.getStatus() == Status.ACTIVE)
                .orElseThrow(()-> new AuthException(ErrorCode.USER_NOT_FOUND));

        if (!userPasswordUpdateRequestDto.getPassword().equals(userPasswordUpdateRequestDto.getPasswordConfirm())) {
            throw new AuthException(ErrorCode.PASSWORD_MISMATCH);
        }

        if(user.getPassword() != null) {
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

        String userStatus;
        Integer unitNo = null;
        if (user.getHousehold() != null) {
            userStatus = "RESIDENT";
            unitNo = user.getHousehold().getUnitNo();
        } else {
            boolean hasPending = approvalRepository.existsByRequestUserIdAndApprovalTypeAndStatus(
                    user, ApprovalType.RESIDENT, ApprovalStatus.PENDING);
            userStatus = hasPending ? "PENDING" : "NONE";
        }

        return UserMeResponseDto.builder()
                .email(user.getEmail())
                .name(user.getName())
                .phone(user.getPhone())
                .birth(user.getBirth())
                .hasLocalPassword(user.getPassword() != null)
                .hasKakao(hasKakao)
                .hasNaver(hasNaver)
                .userStatus(userStatus)
                .unitNo(unitNo)
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
