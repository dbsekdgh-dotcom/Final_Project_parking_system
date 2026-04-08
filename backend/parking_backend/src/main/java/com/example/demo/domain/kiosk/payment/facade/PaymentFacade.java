package com.example.demo.domain.kiosk.payment.facade;

import com.example.demo.domain.kiosk.payment.dtos.internal.PaymentEligibilityResult;
import com.example.demo.domain.kiosk.payment.dtos.request.PaymentConfirmRequestDto;
import com.example.demo.domain.kiosk.payment.dtos.request.SettlementRequestDto;
import com.example.demo.domain.kiosk.payment.dtos.response.PaymentReadyResponseDto;
import com.example.demo.domain.kiosk.payment.dtos.response.SettlementResponseDto;
import com.example.demo.domain.kiosk.payment.dtos.response.VehiclePaymentResponseDto;
import com.example.demo.domain.kiosk.payment.service.AiServerClient;
import com.example.demo.domain.kiosk.payment.service.PaymentService;
import com.example.demo.domain.kiosk.payment.service.SettlementService;
import com.example.demo.domain.shared.parkinglog.ParkingLog;
import com.example.demo.domain.shared.parkinglog.repository.ParkingLogRepository;
import com.example.demo.domain.shared.payment.Payment;
import com.example.demo.domain.shared.payment.enums.PaymentStatus;
import com.example.demo.domain.shared.payment.repository.PaymentRepository;
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
    private final ParkingLogRepository parkingLogRepository;
    private final PaymentRepository paymentRepository;

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

    public PaymentReadyResponseDto beforePayment(SettlementRequestDto settlementRequestDto){
        // 1. 결제 가능한지 검증(ex.결제 요청 시간부터 경과 시간, 결제 금액 변동 여부 확인)
        ParkingLog parkingLog=settlementService.checkEligibility(settlementRequestDto);
        // 2. 결제 전 Payment insert
        PaymentReadyResponseDto paymentReadyResponseDto=settlementService.insertPayment(parkingLog,settlementRequestDto, PaymentStatus.READY);
        return paymentReadyResponseDto;
    }

    //결제 성공시 ,, 결제 실패,취소는 잠깐만...
    public SettlementResponseDto afterPayment(PaymentConfirmRequestDto paymentConfirmRequestDto){
        List<Payment> payments=paymentRepository.findByExternalPaymentId(paymentConfirmRequestDto.getOrderId());
        ParkingLog parkingLog=payments.getFirst().getParkingLog();
        // 3. Payment insert/savePaymentReceipt
        settlementService.savePaymentReceipt(payments,paymentConfirmRequestDto,PaymentStatus.SUCCESS);

        // 2. userPoint & PointLog update/processPointDeduction
        settlementService.pointProcessOfPayment(parkingLog,payments,paymentConfirmRequestDto);

        //settlementService.pointProcessOfPayment();
        // 4. parking Log 업데이트/updateParkingLogFinal
        // 5. Alram/sendSuccessAlarm
        //6.active

        return null;
    }
}
