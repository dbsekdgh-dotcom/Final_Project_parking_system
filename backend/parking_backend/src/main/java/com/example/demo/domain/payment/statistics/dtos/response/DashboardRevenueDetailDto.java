package com.example.demo.domain.payment.statistics.dtos.response;

import lombok.*;

import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Getter@Setter
@Builder
public class DashboardRevenueDetailDto {
    private String date;
    private String category;
    private long amount;
    private long transactionCount;
}
