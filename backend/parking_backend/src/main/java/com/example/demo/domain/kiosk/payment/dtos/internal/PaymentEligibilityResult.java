package com.example.demo.domain.kiosk.payment.dtos.internal;

import com.example.demo.domain.kiosk.payment.dtos.response.VehiclePaymentResponseDto;
import com.example.demo.domain.shared.parkinglog.ParkingLog;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
//파사드 내부에서 서비스끼리 주고받는 용도
public class PaymentEligibilityResult {
    private final ParkingLog parkingLog; //무료면 null, 아니면 데이터가 있음
    private final VehiclePaymentResponseDto vehiclePaymentResponseDto; // 무료면 데이터가 있고, 아니면 null

    public boolean isFree() {
        return vehiclePaymentResponseDto != null && vehiclePaymentResponseDto.isFree();
    }
}
