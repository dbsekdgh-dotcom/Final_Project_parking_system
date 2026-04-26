package com.example.demo.domain.payment.statistics.dtos.internal;

import com.example.demo.domain.payment.enums.PaymentType;
import lombok.*;

import java.time.LocalDate;

public interface DailyRevenueByTypeDto {
    LocalDate getDate();
    Long getRevenue();
    Long getRefund();
    PaymentType getPaymentType();

}
