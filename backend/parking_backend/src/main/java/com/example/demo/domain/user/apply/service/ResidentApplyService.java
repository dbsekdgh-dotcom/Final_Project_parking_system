package com.example.demo.domain.user.apply.service;

import com.example.demo.domain.shared.activityLog.ActivityLog;
import com.example.demo.domain.shared.activityLog.enums.ActivityType;
import com.example.demo.domain.shared.activityLog.repository.ActivityLogRepository;
import com.example.demo.domain.shared.approval.Approval;
import com.example.demo.domain.shared.approval.enums.ApprovalStatus;
import com.example.demo.domain.shared.approval.enums.ApprovalType;
import com.example.demo.domain.shared.approval.repository.ApprovalRepository;
import com.example.demo.domain.shared.household.enums.IsActive;
import com.example.demo.domain.shared.household.repository.HouseholdRepository;
import com.example.demo.domain.shared.user.User;
import com.example.demo.domain.shared.user.UserRepository;
import com.example.demo.domain.user.apply.dtos.request.ResidentApplyRequestDto;
import com.example.demo.domain.user.apply.dtos.response.ResidentApplyCancelResponseDto;
import com.example.demo.domain.user.apply.dtos.response.ResidentApplyResponseDto;
import com.example.demo.domain.user.apply.dtos.response.UserStatusResponseDto;
import com.example.demo.global.exception.CustomException;
import com.example.demo.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ResidentApplyService {

    private final ApprovalRepository approvalRepository;
    private final HouseholdRepository householdRepository;
    private final UserRepository userRepository;
    private final ActivityLogRepository activityLogRepository;

    /**
     * 입주 신청 등록
     */
    @Transactional
    public ResidentApplyResponseDto apply(Long userId, ResidentApplyRequestDto residentApplyRequestDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (user.getHousehold() != null) {
            throw new CustomException(ErrorCode.ALREADY_RESIDENT);
        }

        if (approvalRepository.existsByRequestUserIdAndApprovalTypeAndStatus(user, ApprovalType.RESIDENT, ApprovalStatus.PENDING)) {
            throw new CustomException(ErrorCode.ALREADY_APPLIED_RESIDENT);
        }

        var household = householdRepository.findById(residentApplyRequestDto.getHouseholdId())
                .orElseThrow(() -> new CustomException(ErrorCode.HOUSEHOLD_NOT_FOUND));

        if (household.getIsActive() == IsActive.ACTIVE) {
            throw new CustomException(ErrorCode.HOUSEHOLD_ALREADY_ACTIVE);
        }

        boolean isUnitAlreadyPending = approvalRepository.existsByTargetIdAndApprovalTypeAndStatus(
                household.getHouseholdId(), ApprovalType.RESIDENT, ApprovalStatus.PENDING);

        if (isUnitAlreadyPending) {
            throw new CustomException(ErrorCode.NOT_AVAILABLE_HOUSEHOLD);
        }

        activityLogRepository.save(ActivityLog.builder()
                .activityType(ActivityType.RESIDENT_REGISTERED)
                .household(household)
                .message(String.format("입주민 신청 접수 : %d호 (신청자: %s)",
                        household.getUnitNo(), user.getName()))
                .build());

        Approval approval = Approval.builder()
                .approvalType(ApprovalType.RESIDENT)
                .targetId(household.getHouseholdId())
                .requestUserId(user)
                .status(ApprovalStatus.PENDING)
                .build();

        Approval savedApproval = approvalRepository.save(approval);
        return new ResidentApplyResponseDto(savedApproval);
    }

    /**
     * 입주 신청 취소
     */
    @Transactional
    public ResidentApplyCancelResponseDto cancel(Long userId, Long approvalId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Approval approval = approvalRepository.findByApprovalIdAndRequestUserId_UserId(approvalId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.APPLY_NOT_FOUND));

        if (approval.getApprovalType() != ApprovalType.RESIDENT) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        // 💡 예외 처리 추가: PENDING 상태가 아닐 경우(이미 승인/거절/취소됨) 취소 불가
        if (approval.getStatus() != ApprovalStatus.PENDING) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
            // 또는 상세하게 ErrorCode.ALREADY_PROCESSED 등을 사용 가능
        }

        approval.setStatus(ApprovalStatus.CANCELLED);
        return new ResidentApplyCancelResponseDto(approval);
    }

    /**
     * 마이페이지용 유저 상태 조회
     */
    @Transactional(readOnly = true)
    public UserStatusResponseDto getUserStatus(Long userId) {

        // 윤진추가: userId가 null이면 바로 "NONE"상태로 돌려보내기
        if(userId ==null){
            return UserStatusResponseDto.builder()
                    .userStatus("NONE")
                    .activeApprovalId(null)
                    .build();
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 대기 중인 신청 건 조회
        Long activeId = approvalRepository.findTopByRequestUserIdAndApprovalTypeAndStatusOrderByCreatedAtDesc(
                        user, ApprovalType.RESIDENT, ApprovalStatus.PENDING)
                .map(Approval::getApprovalId)
                .orElse(null);

        // 상태 코드 결정 (리포지토리 스타일과 통일)
        String statusCode;
        if (user.getHousehold() != null) {
            statusCode = "RESIDENT"; // 입주 완료 (OCCUPIED와 일맥상통)
        } else if (activeId != null) {
            statusCode = "PENDING";  // 신청 대기 중
        } else {
            statusCode = "NONE";     // 아무 상태 아님 (AVAILABLE 상태의 방을 신청할 수 있는 유저)
        }

        return UserStatusResponseDto.builder()
                .userStatus(statusCode)
                .activeApprovalId(activeId)
                .build();
    }
}