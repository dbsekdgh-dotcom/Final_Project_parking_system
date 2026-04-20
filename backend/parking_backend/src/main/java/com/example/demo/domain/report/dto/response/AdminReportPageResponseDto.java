package com.example.demo.domain.report.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class AdminReportPageResponseDto {
    private List<AdminReportResponseDto> content;
    private int totalPages;
    private long totalElements;
    private int number;
    private AdminReportStatsResponseDto stats;
}
