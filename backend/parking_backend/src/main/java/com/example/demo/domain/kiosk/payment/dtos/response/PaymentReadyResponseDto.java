package com.example.demo.domain.kiosk.payment.dtos.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Builder
@Getter
@Setter
@ToString
public class PaymentReadyResponseDto {
    private String orderId;
    private Integer amount;
    private String orderName;
    private boolean isPaymentRequired;
    private long parkingLogId;
    private String vehicleNumber;
    private String userEmail;

}
