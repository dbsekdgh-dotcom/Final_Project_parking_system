package com.example.demo.domain.user.mypage.dashboard.service;

import com.example.demo.domain.shared.approval.enums.ApprovalStatus;
import com.example.demo.domain.shared.approval.enums.ApprovalType;
import com.example.demo.domain.shared.approval.repository.ApprovalRepository;
import com.example.demo.domain.shared.user.User;
import com.example.demo.domain.shared.user.enums.Status;
import com.example.demo.domain.user.mypage.dashboard.dto.response.MyPageDashboardResponseDto;
import com.example.demo.domain.user.mypage.dashboard.dto.request.MyPageDashboardUpdateRequestDto;
import com.example.demo.domain.user.mypage.dashboard.repository.UserDashboardRepository;
import com.example.demo.global.exception.AuthException;
import com.example.demo.global.exception.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class MyPageDashboardService {

    private final UserDashboardRepository userDashboardRepository;
    private final ApprovalRepository approvalRepository;
    private final PasswordEncoder passwordEncoder;

    // 1. 회원정보 조회
    public MyPageDashboardResponseDto getUserInfo(Long userId) {
        User user = userDashboardRepository.findById(userId)
                .filter(u -> u.getStatus() == Status.ACTIVE)
                .orElseThrow(() -> new AuthException(ErrorCode.USER_NOT_FOUND));

        // 유저 상태 판별
        String userStatus;
        if (user.getHousehold() != null) {
            userStatus = "입주민";
        } else if (approvalRepository.existsByRequestUserIdAndApprovalTypeAndStatus(
                user, ApprovalType.RESIDENT, ApprovalStatus.PENDING)) {
            userStatus = "입주민 신청 중";
        } else {
            userStatus = "일반 회원";
        }

        return new MyPageDashboardResponseDto(
                user.getName(),
                user.getPhone(),
                user.getBirth(),
                userStatus
        );
    }

    // 2. 회원 정보 수정 (전화번호, 생일 각각 독립적으로 수정 가능)
    public void updateProfile(Long memberId, MyPageDashboardUpdateRequestDto dto) {
        User user = userDashboardRepository.findById(memberId)
                .filter(u -> u.getStatus() == Status.ACTIVE)
                .orElseThrow(() -> new AuthException(ErrorCode.USER_NOT_FOUND));

        if (dto.getPhone() != null && !dto.getPhone().isBlank()) {
            if (userDashboardRepository.existsByPhone(dto.getPhone()) && !user.getPhone().equals(dto.getPhone())) {
                throw new AuthException(ErrorCode.PHONE_DUPLICATE);
            }
            user.updatePhone(dto.getPhone());
        }

        if (dto.getBirth() != null) {
            user.updateBirth(dto.getBirth());
        }
    }

    // 3. 회원탈퇴
    public void withdraw(Long memberId) {
        User user = userDashboardRepository.findById(memberId)
                .filter(m -> m.getStatus() == Status.ACTIVE)
                .orElseThrow(() -> new AuthException(ErrorCode.USER_NOT_FOUND));
        user.setStatus(Status.DELETED);
        user.setDeletedAt(LocalDateTime.now());
    }
}
