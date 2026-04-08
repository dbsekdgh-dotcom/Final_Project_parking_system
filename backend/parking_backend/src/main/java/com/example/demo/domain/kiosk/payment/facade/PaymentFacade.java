package com.example.demo.domain.kiosk.payment.facade;

import com.example.demo.domain.kiosk.payment.dtos.internal.PaymentEligibilityResult;
import com.example.demo.domain.kiosk.payment.dtos.response.VehiclePaymentResponseDto;
import com.example.demo.domain.kiosk.payment.service.AiServerClient;
import com.example.demo.domain.kiosk.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentFacade {
    private final AiServerClient aiServerClient;
    private final PaymentService paymentService;

    public VehiclePaymentResponseDto paymentProcess(Long parkingLogID) {
        //1. 무료 대상인지 확인
        PaymentEligibilityResult paymentEligibilityResult = paymentService.checkFreeExitEligibility(parkingLogID);
        //2. 무료 대상이라면 리턴
        if (paymentEligibilityResult.isFree()) {
            return paymentEligibilityResult.getVehiclePaymentResponseDto();
        }
        //3. 실제 요금정산 및 DB 작업 수행
        return paymentService.requestPayment(paymentEligibilityResult.getParkingLog());
    }
}
