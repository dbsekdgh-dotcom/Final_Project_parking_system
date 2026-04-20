package com.example.demo.domain.report.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminReportStatsResponseDto {
    private long pendingCount;
    private long approvedCount;
    private long rejectedCount;
}
