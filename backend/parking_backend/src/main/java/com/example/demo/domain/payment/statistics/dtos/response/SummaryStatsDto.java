package com.example.demo.domain.payment.statistics.dtos.response;

public interface SummaryStatsDto {
    Long getRevenue();
    Long getRefund();
    Long getPayAmount();
    Long getPointAmount();
    Long getPayRefund();
    Long getPointRefund();
}
