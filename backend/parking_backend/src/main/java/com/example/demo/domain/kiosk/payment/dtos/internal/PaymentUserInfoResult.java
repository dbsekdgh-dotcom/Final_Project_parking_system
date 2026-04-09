package com.example.demo.domain.kiosk.payment.dtos.internal;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class PaymentUserInfoResult {
    private long parkingLogId;
    private long vehicleId;
    private long userId;
    private int currentPoint;

}
