package com.example.demo.domain.payment.dtos.response;

import com.example.demo.domain.payment.dtos.internal.AppliedTicketResult;
import com.example.demo.domain.payment.dtos.internal.StackableTicketResult;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@ToString
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class VehiclePaymentResponseDto {
    private long parkingLogId;
    private boolean isFree;
    private String message;
    private String vehicleNumber;
    private long parkingTime;
    private Integer rawFee;          // 10,000 (할인 전 원래 요금 - 화면엔 안 보이지만 로그용)
    private long calculatedFee;        // 5,000 (누적금액 )
    private long amountToPay;           //실제 결제 금액
    private StackableTicketResult stackableTicketResult;
    private int totalDiscountMinutes;
    private int totalDiscountAmount;
}
