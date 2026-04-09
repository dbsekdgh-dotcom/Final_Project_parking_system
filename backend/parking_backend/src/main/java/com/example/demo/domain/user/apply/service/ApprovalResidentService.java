package com.example.demo.domain.user.apply.service;

import com.example.demo.domain.shared.approval.Approval;
import com.example.demo.domain.shared.approval.enums.ApprovalType;
import com.example.demo.domain.shared.approval.repository.ApprovalRepository;
import com.example.demo.domain.shared.household.repository.HouseholdRepository;
import com.example.demo.domain.shared.user.User;
import com.example.demo.domain.shared.user.UserRepository;
import com.example.demo.domain.user.apply.dtos.request.ApprovalResidentRequestDto;
import com.example.demo.domain.user.apply.dtos.response.ApprovalResidentResponseDto;
import com.example.demo.global.exception.CustomException;
import com.example.demo.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.example.demo.domain.shared.household.enums.IsActive;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ApprovalResidentService {

    private final ApprovalRepository approvalRepository;
    private final HouseholdRepository householdRepository;
    private final UserRepository userRepository;

    @Transactional
    public ApprovalResidentResponseDto apply(Long userId, ApprovalResidentRequestDto approvalResidentRequestDto) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (approvalRepository.existsByRequestUserIdAndApprovalType(user, ApprovalType.RESIDENT)) {
            throw new CustomException(ErrorCode.ALREADY_APPLIED_RESIDENT);
        }

        var household = householdRepository.findById(approvalResidentRequestDto.getHouseholdId())
                .orElseThrow(()-> new CustomException(ErrorCode.ENTITY_NOT_FOUND));

        if (household.getIsActive() == IsActive.ACTIVE ) {
            throw new CustomException(ErrorCode.HOUSEHOLD_ALREADY_ACTIVE);
        }

        Approval approval = Approval.builder()
                .approvalType(ApprovalType.RESIDENT)
                .targetId(household.getHouseholdId())
                .requestUserId(user)
                .build();

        return new ApprovalResidentResponseDto(approvalRepository.save(approval));
    }
}
