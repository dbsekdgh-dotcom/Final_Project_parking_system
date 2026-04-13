package com.example.demo.domain.kiosk.payment.dtos.internal;

import com.example.demo.domain.shared.parkinglog.enums.PaymentStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Builder
@Getter
public class ParkingLogRefundDto {
    private PaymentStatus paymentStatus;
    private int fee;
    private LocalDateTime paidAt;
    private LocalDateTime freeExitUntil;
    private LocalDateTime paymentRequestedAt;
}

