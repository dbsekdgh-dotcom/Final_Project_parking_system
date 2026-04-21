package com.example.demo.domain.payment.subscription.dtos.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SubscriptionRefundResponseDto {

    private int cashRefundAmount;      // 실제 환불된 현금액
    private int pointRefundAmount;     // 반환된 포인트 (포인트/혼합 결제 시)
    private int revokedPoint;          // 회수된 적립 포인트
    private int pointDeductedAsCash;   // 포인트 잔고 부족으로 현금에서 추가 공제된 금액
}
