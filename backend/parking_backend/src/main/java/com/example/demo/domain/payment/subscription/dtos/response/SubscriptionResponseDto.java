package com.example.demo.domain.payment.subscription.dtos.response;

import com.example.demo.domain.payment.subscription.Subscription;
import com.example.demo.domain.payment.subscription.enums.Status;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class SubscriptionResponseDto {
    private Long subscriptionId;
    private String carNumber;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Status status;
    private int price;
    private int earnedPoint;
    private int paidAmount;        // 현금 결제분
    private int usedPoint;         // 포인트 결제분
    private LocalDateTime createdAt;    // 결제일
    private LocalDateTime cancelledAt;  // 환불일 (REFUNDED) / 취소일 (CANCELLED)

    public static SubscriptionResponseDto from(Subscription subscription, int earnedPoint) {
        int paid = subscription.getPayment() != null
                ? subscription.getPayment().getAmount().intValue() : 0;
        int used = subscription.getPrice() - paid;
        return SubscriptionResponseDto.builder()
                .subscriptionId(subscription.getSubscriptionId())
                .carNumber(subscription.getVehicle().getCarNumber())
                .startDate(subscription.getStartDate())
                .endDate(subscription.getEndDate())
                .status(subscription.getStatus())
                .price(subscription.getPrice())
                .earnedPoint(earnedPoint)
                .paidAmount(paid)
                .usedPoint(used)
                .createdAt(subscription.getCreatedAt())
                .cancelledAt(subscription.getCancelledAt())
                .build();
    }
}