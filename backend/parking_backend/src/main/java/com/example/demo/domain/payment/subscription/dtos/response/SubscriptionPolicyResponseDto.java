package com.example.demo.domain.payment.subscription.dtos.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SubscriptionPolicyResponseDto {

    private long price;
    private int durationDays;
    private int maxCount;
    private long activeCount;
    private long remainCount;
}