package com.example.demo.domain.payment.statistics.dtos.response;

import java.time.LocalDate;

public class DashboardRevenueDetailDto {
    private LocalDate date;
    private String category;
    private long amount;
    private long transactionCount;
}
