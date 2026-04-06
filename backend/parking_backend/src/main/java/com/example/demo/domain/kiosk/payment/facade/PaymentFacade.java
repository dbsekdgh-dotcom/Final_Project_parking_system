package com.example.demo.domain.kiosk.payment.facade;

import com.example.demo.domain.kiosk.payment.dtos.request.VehiclePaymentRequestDto;
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

    public VehiclePaymentResponseDto paymentProcess(VehiclePaymentRequestDto vehiclePaymentRequestDto){
        //1. 무료 대상인지 확인
        VehiclePaymentResponseDto vehiclePaymentResponseDto =paymentService.checkFreeExitEligibility(vehiclePaymentRequestDto);
        //2. 무료 대상이라면 리턴
        if(vehiclePaymentResponseDto !=null){
            return null;
        }
        //3. 무료 대상이 아니라면 AI 서버 락 요청
        aiServerClient.checkPaymentLock(vehiclePaymentRequestDto.getVehicleNumber());
        //4. 실제 요금정산 및 DB 작업 수행
        return paymentService.requestPayment(vehiclePaymentRequestDto);
    }
}
