package com.example.demo.domain.dashboard.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class DashboardUsageResponseDto {
    private long totalCount;
    private double changePercent;
    private List<MonthlyCountDto> monthly;
}
