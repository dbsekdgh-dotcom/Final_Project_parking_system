package com.example.demo.domain.user.apply.dtos.response;


import com.example.demo.domain.shared.approval.Approval;
import com.example.demo.domain.shared.approval.enums.ApprovalStatus;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ResidentApplyCancelResponseDto {

    private final Long approvalId;
    private final Long householdId;
    private final ApprovalStatus status;
    private final LocalDateTime cancelledAt;
    private final String message;

    public ResidentApplyCancelResponseDto(Approval approval) {
        this.approvalId = approval.getApprovalId();
        this.householdId = approval.getTargetId();
        this.status = approval.getStatus();
        this.cancelledAt = LocalDateTime.now();
        this.message = "입주 신청 취소가 정상적으로 처리되었습니다.";
    }
}
