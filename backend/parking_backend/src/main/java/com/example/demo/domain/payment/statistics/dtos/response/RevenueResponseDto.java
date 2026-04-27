package com.example.demo.domain.payment.statistics.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RevenueResponseDto {
    private long revenue;
    private long refund;
    private long cost;
    private long net;
    private List<DailyStatsDto> dailyStats;
}
