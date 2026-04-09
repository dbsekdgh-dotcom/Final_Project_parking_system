package com.example.demo.domain.user.apply.dtos.response;

import com.example.demo.domain.shared.approval.Approval;
import lombok.Getter;

@Getter
public class ApprovalResidentResponseDto {
    private final Long approvalId;
    private final String status;
    private final String approvalType;

    public ApprovalResidentResponseDto(Approval approval) {
        this.approvalId = approval.getApprovalId();
        this.status = approval.getStatus().name();
        this.approvalType = approval.getApprovalType().getDescription();
    }
}
