package com.example.demo.domain.payment.statistics.dtos.response;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class RevenueAnalysisDto {
    private long amount;
    private long exitCount;
    private long card;
    private long point;
    private long ticket;
    private List<DailyRevenueDto> dailyRevenue;
}
