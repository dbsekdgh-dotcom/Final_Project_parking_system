package com.example.demo.domain.kiosk.payment.dtos.response;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Builder
@ToString
public class VehiclePaymentResponseDto {
    private boolean isFree;
    private String message;
    private String vehicleNumber;
    private long parkingTime;
    private Integer rawFee;          // 10,000 (할인 전 원래 요금 - 화면엔 안 보이지만 로그용)
    private long calculatedFee;        // 5,000 (누적금액 )
    private long amountToPay;           //실제 결제 금액
}
