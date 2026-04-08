package com.example.demo.domain.kiosk.payment.facade;

import com.example.demo.domain.kiosk.payment.dtos.internal.PaymentEligibilityResult;
import com.example.demo.domain.kiosk.payment.dtos.request.SettlementRequestDto;
import com.example.demo.domain.kiosk.payment.dtos.response.SettlementResponseDto;
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

    public SettlementResponseDto confirmPayment(SettlementRequestDto settlementRequestDto){
        // 1. user & parkingLog 검증(유저의 포인트가 요청된 포인트보다 많은지, 주차 로그가 결제 가능한 상태인지)
        // 2. userPoint & PointLog update
        // 3. Payment insert
        // 4. parking Log 업데이트
        // 5. Alram
        return null;
    }
}
