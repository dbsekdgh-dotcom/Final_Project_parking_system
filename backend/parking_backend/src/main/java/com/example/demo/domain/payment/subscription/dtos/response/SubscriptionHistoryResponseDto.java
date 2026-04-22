package com.example.demo.domain.payment.subscription.dtos.response;

import com.example.demo.domain.payment.subscription.Subscription;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class SubscriptionHistoryResponseDto {

    private Long subscriptionId;
    private String carNumber;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String status;
    private int price;

    public static SubscriptionHistoryResponseDto from(Subscription subscription) {
        return SubscriptionHistoryResponseDto.builder()
                .subscriptionId(subscription.getSubscriptionId())
                .carNumber(subscription.getVehicle().getCarNumber())
                .startDate(subscription.getStartDate())
                .endDate(subscription.getEndDate())
                .status(subscription.getStatus().name())
                .price(subscription.getPrice())
                .build();
    }
}
