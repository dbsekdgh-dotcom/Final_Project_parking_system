package com.example.demo.domain.payment.subscription.dtos.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SubscriptionRefundResponseDto {

    private long refundAmount;   // 실제 환불된 금액
    private String refundType;   // "전액환불" 또는 "부분환불"
    private String message;      // 사용자에게 보여줄 안내 문구
}
