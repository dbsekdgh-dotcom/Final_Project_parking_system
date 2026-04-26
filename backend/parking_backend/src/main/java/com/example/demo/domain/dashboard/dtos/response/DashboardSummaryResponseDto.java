package com.example.demo.domain.dashboard.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DashboardSummaryResponseDto {
    private long householdCount;
    private long vehicleCount;
    private long parkingSpaceCount;
    private long totalRevenue;
}
