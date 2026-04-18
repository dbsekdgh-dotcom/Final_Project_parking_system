package com.example.demo.domain.payment.dtos.response;

import com.example.demo.domain.payment.dtos.internal.AppliedTicketResult;
import com.example.demo.domain.payment.dtos.internal.StackableTicketResult;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

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
    private String vehicleNumber;
    private StackableTicketResult stackableTicketResult;
}
