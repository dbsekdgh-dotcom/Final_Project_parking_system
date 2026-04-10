package com.example.demo.domain.kiosk.payment.dtos.request;

import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentConfirmRequestDto {
    private String paymentKey; //토스에서 보낸 고유 키
    private String orderId;  //만든 uuid
    private long amount;  //실제 결제 금액
    private long parkingLogId;
}
