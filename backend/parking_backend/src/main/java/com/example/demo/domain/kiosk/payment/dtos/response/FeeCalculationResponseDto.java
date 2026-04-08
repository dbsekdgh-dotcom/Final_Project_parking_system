package com.example.demo.domain.kiosk.payment.dtos.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FeeCalculationResponseDto {
    private int rawFee;
    private long calculatedFee;
    private int totalDiscountMinutes;
    private int totalDiscountAmount;
    private long parkingTime;
    private long amountToPay;
    private LocalDateTime paymentRequestedAt;
}
