package com.example.demo.domain.kiosk.payment.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FeeCalculationResponseDto {
    private final int rawFee;
    private final int calculdatedFee;
}
