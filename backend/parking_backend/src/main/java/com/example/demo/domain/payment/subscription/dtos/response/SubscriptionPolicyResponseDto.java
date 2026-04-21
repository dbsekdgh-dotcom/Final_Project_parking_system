package com.example.demo.domain.payment.subscription.dtos.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SubscriptionPolicyResponseDto {

    private long price;        // 정기권 금액 (SUB_MONTHLY_PRICE)
    private int durationDays;  // 이용 기간 (SUB_DURATION_DAYS)
    private long maxCount;     // 최대 판매 수량 (SUB_MAX_COUNT)
    private long remainCount;  // 현재 구매 가능한 남은 슬롯 수
}
