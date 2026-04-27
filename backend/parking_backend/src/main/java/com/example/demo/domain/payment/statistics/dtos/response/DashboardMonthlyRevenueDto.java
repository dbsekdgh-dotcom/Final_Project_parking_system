package com.example.demo.domain.payment.statistics.dtos.response;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter@Setter
public class DashboardMonthlyRevenueDto {
    private String month;
    private long amount;
}
