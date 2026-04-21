package com.example.demo.domain.payment.subscription.dtos.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class SubscriptionPurchaseRequestDto {
    private Long vehicleId;
    private LocalDateTime startDate;
    private int usedPoint;
    private int paidAmount;
    private String paymentKey;
    private String orderId;
}
