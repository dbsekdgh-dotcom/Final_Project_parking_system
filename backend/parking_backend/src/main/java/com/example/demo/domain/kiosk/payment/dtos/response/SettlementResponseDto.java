package com.example.demo.domain.kiosk.payment.dtos.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class SettlementResponseDto {
    private String paymentStatus;
    private String vehicleNumber;
    private Integer paidAmount;
    private String exitDeadline;
    private String message;
}
