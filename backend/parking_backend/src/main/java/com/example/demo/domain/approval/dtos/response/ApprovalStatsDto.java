package com.example.demo.domain.approval.dtos.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ApprovalStatsDto {
    private long pendingCount;
    private long approvedCount;
    private long rejectedCount;
}
