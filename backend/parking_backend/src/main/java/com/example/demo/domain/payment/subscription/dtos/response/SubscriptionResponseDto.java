package com.example.demo.domain.payment.subscription.dtos.response;

import com.example.demo.domain.payment.subscription.Subscription;
import com.example.demo.domain.payment.subscription.enums.Status;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

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
    private int paidAmount;
    private int usedPoint;
    private int remainingDays;
    private LocalDateTime createdAt;
    private LocalDateTime cancelledAt;

    public static SubscriptionResponseDto from(Subscription subscription, int earnedPoint) {
        int paid = subscription.getPayment() != null
                ? subscription.getPayment().getAmount().intValue() : 0;
        int used = subscription.getPrice() - paid;

        int remainingDays = 0;
        if (subscription.getStatus() == Status.ACTIVE && subscription.getEndDate() != null) {
            long days = ChronoUnit.DAYS.between(LocalDateTime.now(), subscription.getEndDate());
            remainingDays = (int) Math.max(0, days);
        }

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
                .remainingDays(remainingDays)
                .createdAt(subscription.getCreatedAt())
                .cancelledAt(subscription.getCancelledAt())
                .build();
    }
}