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
import com.example.demo.domain.shared.activityLog.enums.ActivityType;
import com.example.demo.domain.shared.parkinglog.ParkingLog;
import com.example.demo.domain.shared.payment.Payment;
import com.example.demo.domain.shared.payment.enums.PaymentStatus;
import com.example.demo.domain.shared.payment.repository.PaymentRepository;
import com.example.demo.domain.shared.user.User;
import com.example.demo.global.exception.BusinessException;
import com.example.demo.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentFacade {
    private final AiServerClient aiServerClient;
    private final PaymentService paymentService;
    private final SettlementService settlementService;
    private final PaymentRepository paymentRepository;

    public VehiclePaymentResponseDto paymentProcess(Long parkingLogID) {
        try {
            //1. 무료 대상인지 확인
            PaymentEligibilityResult paymentEligibilityResult = paymentService.checkFreeExitEligibility(parkingLogID);
            ParkingLog parkingLog=paymentEligibilityResult.getParkingLog();
            //2. 무료 대상이라면 리턴
            if (paymentEligibilityResult.isFree()) {
                return paymentEligibilityResult.getVehiclePaymentResponseDto();
            }
            //3. 락 걸기
            aiServerClient.requestPaymentLock(parkingLog.getCarNumberSnapshot());
            //4. 실제 요금정산 및 DB 작업 수행
            VehiclePaymentResponseDto vehiclePaymentResponseDto= paymentService.requestPayment(parkingLog);
            if(vehiclePaymentResponseDto.isFree()){
                //계산시 할인 등으로 인해 무료인 경우
                SettlementResponseDto settlementResponseDto= settlementService.updateParkingLogFinal(parkingLog,0);
                vehiclePaymentResponseDto.setMessage(settlementResponseDto.getExitDeadline()+"까지 출차해주세요");
            }
            return vehiclePaymentResponseDto;
        }catch (BusinessException e){
            e.printStackTrace();
            throw e;
        }catch (Exception e){
            log.error("정산 초기화 중 예상치 못한 에러: {}",e.getMessage());
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    //결제 요청 시 사전 검증
    public PaymentReadyResponseDto beforePayment(SettlementRequestDto settlementRequestDto){
        // 1. 결제 가능한지 검증(ex.결제 요청 시간부터 경과 시간, 결제 금액 변동 여부 확인)
        ParkingLog parkingLog=settlementService.checkEligibility(settlementRequestDto);
        // 2. 결제 전 Payment insert
        PaymentReadyResponseDto paymentReadyResponseDto=settlementService.insertPayment(parkingLog,settlementRequestDto, PaymentStatus.READY);
        return paymentReadyResponseDto;
    }

    //결제 성공/실패/취소 시
    public SettlementResponseDto afterPayment(PaymentConfirmRequestDto paymentConfirmRequestDto, ActivityType activityType,PaymentStatus paymentStatus){
        List<Payment> payments=paymentRepository.findByExternalPaymentId(paymentConfirmRequestDto.getOrderId());
        if(payments.isEmpty()){
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }
        ParkingLog parkingLog=payments.getFirst().getParkingLog();
        User user=parkingLog.getVehicle().getUser();
        SettlementResponseDto settlementResponseDto=null;
        String carNumber=parkingLog.getCarNumberSnapshot();
        try {
            // 결제 성공 시
            if (PaymentStatus.SUCCESS.equals(paymentStatus)) {
                // 1. Payment insert
                settlementService.savePaymentReceipt(payments, paymentConfirmRequestDto, paymentStatus);
                // 2. userPoint & PointLog update
                settlementService.pointProcessOfPayment(user, parkingLog, payments, paymentConfirmRequestDto);
                // 3. parking Log 업데이트
                settlementResponseDto = settlementService.updateParkingLogFinal(parkingLog, paymentConfirmRequestDto.getAmount());
                // 4. notification insert
                settlementService.insertNotification(user, settlementResponseDto.getExitDeadline());
                // 5. active log insert(포인트+카드 결제면 두줄?)// 사전정산인지, 출차 정산인지 여부는 컨트롤러에서
                settlementService.insertActivityLog(parkingLog, user, payments, activityType);
                //결제 실패 시
            } else if (PaymentStatus.FAILED.equals(paymentStatus)) {
                // 1. Payment insert
                settlementService.savePaymentReceipt(payments, paymentConfirmRequestDto, paymentStatus);
                // 2. 리액트에 에러 반환
                throw new BusinessException(ErrorCode.PAYMENT_NOT_COMPLETED);
                //결제 취소 시
            } else if (PaymentStatus.CANCELLED.equals(paymentStatus)) {
                // 1. Payment insert
                settlementService.savePaymentReceipt(payments, paymentConfirmRequestDto, paymentStatus);
                // 2. return
                settlementResponseDto = SettlementResponseDto.builder()
                        .paymentStatus(paymentStatus.name())
                        .vehicleNumber(carNumber)
                        .build();
            }
        }catch (BusinessException e){
            throw e;
        }catch(Exception e){
            log.error("결제정보 처리 중 에러 :{}",e.getMessage());
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }finally {
            //lock해제
            aiServerClient.requestPaymentLockRelease(carNumber);
        }

        return settlementResponseDto;
    }
}
