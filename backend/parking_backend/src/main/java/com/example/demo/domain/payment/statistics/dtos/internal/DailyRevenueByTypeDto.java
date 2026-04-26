package com.example.demo.domain.payment.statistics.dtos.internal;

import lombok.*;

import java.time.LocalDate;

public interface DailyRevenueByTypeDto {
    LocalDate getDate();
    Long getRevenue();
    Long getRefund();

}
