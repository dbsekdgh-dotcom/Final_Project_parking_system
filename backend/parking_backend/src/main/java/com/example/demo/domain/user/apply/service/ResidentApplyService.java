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
import com.example.demo.domain.user.apply.dtos.response.ResidentApplyResponseDto;
import com.example.demo.global.exception.CustomException;
import com.example.demo.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ResidentApplyService {

    private final ApprovalRepository approvalRepository;
    private final HouseholdRepository householdRepository;
    private final UserRepository userRepository;
    private final ActivityLogRepository activityLogRepository;

    @Transactional
    public ResidentApplyResponseDto apply(Long userId, ResidentApplyRequestDto residentApplyRequestDto) {

        // 1. 사용자 존재 여부 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 2. 중복 신청 확인 (상태가 PENDING인 건이 있는지 체크)
        // 만약 기존에 거절(REJECTED)된 적이 있다면 재신청이 가능해야 하므로 Status 조건을 추가했습니다.
        if (approvalRepository.existsByRequestUserIdAndApprovalTypeAndStatus(user, ApprovalType.RESIDENT, ApprovalStatus.PENDING)) {
            throw new CustomException(ErrorCode.ALREADY_APPLIED_RESIDENT);
        }

        // 3. 세대(Household) 정보 확인
        var household = householdRepository.findById(residentApplyRequestDto.getHouseholdId())
                .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND));

        // 4. 이미 활성화된 세대인지 확인 (이미 입주민이 등록된 경우 방지)
        if (household.getIsActive() == IsActive.ACTIVE) {
            throw new CustomException(ErrorCode.HOUSEHOLD_ALREADY_ACTIVE);
        }

        // 5. 활동 로그 기록
        activityLogRepository.save(ActivityLog.builder()
                .activityType(ActivityType.RESIDENT_REGISTERED)
                .household(household)
                .message(String.format("입주민 신청 접수 : %d호 (신청자: %s)",
                        household.getUnitNo(), user.getName()))
                .build());

        // 6. 승인 요청 데이터 생성 (Status는 엔티티 기본값인 PENDING으로 설정됨)
        Approval approval = Approval.builder()
                .approvalType(ApprovalType.RESIDENT)
                .targetId(household.getHouseholdId())
                .requestUserId(user)
                .build();

        // 7. 저장 후 DTO 반환
        Approval savedApproval = approvalRepository.save(approval);
        return new ResidentApplyResponseDto(savedApproval);
    }
}