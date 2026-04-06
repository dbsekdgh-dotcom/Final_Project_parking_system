package com.example.demo.domain.kiosk.payment.dtos.request;

import com.example.demo.domain.shared.parkingfeepolicy.ParkingFeePolicy;
import lombok.Builder;
import lombok.Getter;


@Getter
@Builder
public class FeeCalculationRequestDto {
    private final long parkingTime;
    private final ParkingFeePolicy policy;
    private final int prepaidFee;
    private final int discountAmount;
}
