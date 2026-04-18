package com.example.demo.domain.payment.dtos.response;

import com.example.demo.domain.payment.dtos.internal.StackableTicketResult;
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
