package com.example.demo.domain.kiosk.payment.facade;

import com.example.demo.domain.kiosk.payment.dtos.internal.PaymentEligibilityResult;
import com.example.demo.domain.kiosk.payment.dtos.request.SettlementRequestDto;
import com.example.demo.domain.kiosk.payment.dtos.response.SettlementResponseDto;
import com.example.demo.domain.kiosk.payment.dtos.response.VehiclePaymentResponseDto;
import com.example.demo.domain.kiosk.payment.service.AiServerClient;
import com.example.demo.domain.kiosk.payment.service.PaymentService;
import com.example.demo.domain.kiosk.payment.service.SettlementService;
import com.example.demo.domain.shared.parkinglog.ParkingLog;
import com.example.demo.domain.shared.payment.enums.PaymentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class PaymentFacade {
    private final AiServerClient aiServerClient;
    private final PaymentService paymentService;
    private final SettlementService settlementService;

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

    public Map beforePayment(SettlementRequestDto settlementRequestDto){
        // 1. 무료/유료 정산 대상 업데이트 (검증 =>userpoint&pointLog => payment => parking_log(fee ,calculated_fee,payment_status ,paid_at,free_exit_until)=>알람)
        // 3. Payment insert/savePaymentReceipt
        // 2. userPoint & PointLog update/processPointDeduction
        // 4. parking Log 업데이트/updateParkingLogFinal
        // 5. Alram/sendSuccessAlarm
        //6.active

        // 1. 결제 가능한지 검증(ex.결제 요청 시간부터 경과 시간, 결제 금액 변동 여부 확인)
        ParkingLog parkingLog=settlementService.checkEligibility(settlementRequestDto);
        // 2. 결제 전 Payment insert
        settlementService.insertPayment(parkingLog,settlementRequestDto, PaymentStatus.READY);

        return null;
    }

    public void afterPayment(){

    }
}
