package com.example.demo.domain.approval.dtos.response;

import com.example.demo.domain.approval.Approval;
import com.example.demo.domain.approval.enums.ApprovalStatus;
import com.example.demo.domain.approval.enums.ApprovalType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ApprovalResponseDto {
    private Long approvalId;
    private ApprovalType approvalType;
    private Long targetId;
    private String requestUserName;
    private String content;
    private ApprovalStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime processedAt;
    private boolean processedBySystem;
    private String rejectReason;
    private LocalDateTime visitStartAt;
    private LocalDateTime visitEndAt;

    public static ApprovalResponseDto from(
            Approval approval,
            String content,
            LocalDateTime visitStartAt,
            LocalDateTime visitEndAt
    ){
        boolean processedBySystem =
                approval.getProcessedByAdminId() == null
                && approval.getStatus() != ApprovalStatus.PENDING;
        String requesterName = (approval.getRequestUserId() != null)
                ? approval.getRequestUserId().getName() : "알 수 없음";
        return ApprovalResponseDto.builder()
                .approvalId(approval.getApprovalId())
                .approvalType(approval.getApprovalType())
                .targetId(approval.getTargetId())
                .requestUserName(requesterName)
                .content(content)
                .status(approval.getStatus())
                .createdAt(approval.getCreatedAt())
                .processedAt(approval.getProcessedAt())
                .processedBySystem(processedBySystem)
                .rejectReason(approval.getRejectReason())
                .visitStartAt(visitStartAt)
                .visitEndAt(visitEndAt)
                .build();
    }
}
