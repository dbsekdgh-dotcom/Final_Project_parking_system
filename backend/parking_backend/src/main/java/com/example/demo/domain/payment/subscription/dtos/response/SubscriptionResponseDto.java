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
    private int paidAmount;   // 현금 결제분 (환불 예상액 계산용)
    private int usedPoint;    // 포인트 결제분 (환불 예상액 계산용)

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
                .build();
    }
}