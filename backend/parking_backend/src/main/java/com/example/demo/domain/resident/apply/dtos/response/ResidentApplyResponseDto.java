package com.example.demo.domain.resident.apply.dtos.response;

import com.example.demo.domain.approval.Approval;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "입주 신청 결과 응답 — 생성된 승인 ID, 처리 상태, 신청 유형을 반환합니다.")
public class ResidentApplyResponseDto {
    private final Long approvalId;
    private final String status;
    private final String approvalType;

    public ResidentApplyResponseDto(Approval approval) {
        this.approvalId = approval.getApprovalId();
        this.status = approval.getStatus().name();
        this.approvalType = approval.getApprovalType().getDescription();
    }
}
