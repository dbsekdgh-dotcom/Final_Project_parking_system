package com.example.demo.domain.payment.subscription.dtos.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "정기권 구매 준비 응답 - 검증이 완료된 주문 정보를 반환하며, 이 데이터로 토스 결제창을 호출합니다.")
public class SubscriptionReadyResponseDto {

    private final String orderId;
    private final String orderName;
    private final Long amount;

    private final String carNumber;
    private final LocalDateTime startDate;
    private final LocalDateTime endDate;

}
