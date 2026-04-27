package com.example.demo.domain.dashboard.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DashboardSummaryResponseDto {
    private long householdCount;        // 활성 세대수
    private long totalHouseholdCount;   // 전체 세대수
    private long vehicleCount;
    private long occupiedParkingSpaceCount; // 사용 중 공간
    private long parkingSpaceCount;         // 전체 공간
    private long totalRevenue;
}
