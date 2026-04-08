package com.example.demo.domain.kiosk.payment.dtos.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class PaymentReadyResponseDto {
    private String tempPaymentId;
    private Integer amount;
    private String orderName;
    private boolean isPaymentRequired;

}
