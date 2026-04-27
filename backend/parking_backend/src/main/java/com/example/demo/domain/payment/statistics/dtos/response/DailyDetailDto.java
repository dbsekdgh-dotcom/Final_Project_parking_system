package com.example.demo.domain.payment.statistics.dtos.response;

import java.time.LocalDate;

public interface DailyDetailDto {
    LocalDate getDate();
    long getParkingRevenue();      // 주차 매출 (환불 전)
    long getTicketRevenue();       // 할인권 매출 (환불 전)
    long getSubscriptionRevenue(); // 정기권 매출 (환불 전)
    long getRefund();               // 전체 환불 발생액
    long getPayAmount();
    long getPointAmount();
    long getPayRefund();
    long getPointRefund();
}
