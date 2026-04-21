package com.example.demo.domain.report.dto.request;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminReportRejectRequestDto {
    private String rejectReason;
}
