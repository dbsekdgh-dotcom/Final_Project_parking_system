package com.example.demo.domain.payment.subscription.dtos.response;

import com.example.demo.domain.payment.subscription.Subscription;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Getter
@Builder
public class SubscriptionMyInfoResponseDto {

    private Long subscriptionId;
    private String carNumber;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String status;
    private int price;
    private long remainingDays; // 오늘 기준 남은 이용 일수

    public static SubscriptionMyInfoResponseDto from(Subscription subscription) {
        long remaining = ChronoUnit.DAYS.between(LocalDateTime.now(), subscription.getEndDate());

        return SubscriptionMyInfoResponseDto.builder()
                .subscriptionId(subscription.getSubscriptionId())
                .carNumber(subscription.getVehicle().getCarNumber())
                .startDate(subscription.getStartDate())
                .endDate(subscription.getEndDate())
                .status(subscription.getStatus().name())
                .price(subscription.getPrice())
                .remainingDays(Math.max(remaining, 0)) // 음수 방지
                .build();
    }
}
