package com.example.demo.domain.kiosk.payment.facade;

import com.example.demo.domain.kiosk.payment.dtos.internal.PaymentEligibilityResult;
import com.example.demo.domain.kiosk.payment.dtos.internal.TossApprovalResult;
import com.example.demo.domain.kiosk.payment.dtos.request.PaymentConfirmRequestDto;
import com.example.demo.domain.kiosk.payment.dtos.request.SettlementRequestDto;
import com.example.demo.domain.kiosk.payment.dtos.response.PaymentReadyResponseDto;
import com.example.demo.domain.kiosk.payment.dtos.response.SettlementResponseDto;
import com.example.demo.domain.kiosk.payment.dtos.response.VehiclePaymentResponseDto;
import com.example.demo.domain.kiosk.payment.service.*;
import com.example.demo.domain.shared.activityLog.enums.ActivityType;
import com.example.demo.domain.shared.parkinglog.ParkingLog;
import com.example.demo.domain.shared.parkinglog.repository.ParkingLogRepository;
import com.example.demo.domain.shared.payment.Payment;
import com.example.demo.domain.shared.payment.enums.PaymentStatus;
import com.example.demo.domain.shared.payment.repository.PaymentRepository;
import com.example.demo.global.exception.BusinessException;
import com.example.demo.global.exception.ErrorCode;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentFacade {
    private final AiServerClient aiServerClient;
    private final PaymentService paymentService;
    private final SettlementService settlementService;
    private final PaymentRepository paymentRepository;
    private final ParkingLogRepository parkingLogRepository;
    private final TossPaymentService tossPaymentService;
    private final EntityManager entityManager;
    private final RefundService refundService;

    public VehiclePaymentResponseDto paymentProcess(Long parkingLogID) {
        //1. 무료 대상인지 확인
        PaymentEligibilityResult result = paymentService.checkFreeExitEligibility(parkingLogID);
        ParkingLog parkingLog=result.getParkingLog();
        try {
            //2. 무료 대상이라면 리턴
            if (result.isFree()) {
                return result.getVehiclePaymentResponseDto();
            }
            //3. 락 걸기
            aiServerClient.requestPaymentLock(parkingLog.getCarNumberSnapshot());
            //4. 요금계산
            VehiclePaymentResponseDto responseDto= paymentService.requestPayment(parkingLog);
            if(responseDto.isFree()){
                //계산시 할인 등으로 인해 무료인 경우
                SettlementResponseDto settlementResponseDto= settlementService.updateParkingLogFinal(parkingLog,0);
                responseDto.setMessage(settlementResponseDto.getExitDeadline()+"까지 출차해주세요");
            }
            return responseDto;
        }catch (BusinessException e){
            aiServerClient.requestPaymentLockRelease(parkingLog.getCarNumberSnapshot());
            e.printStackTrace();
            throw e;
        }catch (Exception e){
            log.error("정산 초기화 중 예상치 못한 에러: {}",e.getMessage());
            aiServerClient.requestPaymentLockRelease(parkingLog.getCarNumberSnapshot());
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    //결제 요청 시 사전 검증
    public PaymentReadyResponseDto beforePayment(SettlementRequestDto dto){
        // 1. 결제 대상자 확인
        ParkingLog parkingLog=settlementService.validateVehicleStatus(dto.getParkingLogId());
        // 2. 결제 금액 계산
        VehiclePaymentResponseDto vehiclePaymentResponseDto=paymentService.requestPayment(parkingLog);
        // 3. 결제 전 Payment insert/parkinglog update
        PaymentReadyResponseDto paymentReadyResponseDto=settlementService.insertPayment(parkingLog,dto, PaymentStatus.READY,vehiclePaymentResponseDto);
        // 4. 락 해제
        //aiServerClient.requestPaymentLockRelease(parkingLog.getCarNumberSnapshot());
        return paymentReadyResponseDto;
    }

    //결제 성공/실패/취소 시
    @Transactional
    public SettlementResponseDto afterPayment(PaymentConfirmRequestDto dto, ActivityType activityType){
        // 0. 기초정보 조회
        List<Payment> payments=paymentRepository.findByExternalPaymentId(dto.getOrderId());
        if(payments.isEmpty())throw new BusinessException(ErrorCode.INVALID_REQUEST);
        ParkingLog parkingLog=parkingLogRepository.getDetailLogInfo(dto.getParkingLogId())
                .orElseThrow(()-> new BusinessException(ErrorCode.INVALID_REQUEST));

        String carNumber=parkingLog.getCarNumberSnapshot();
        PaymentStatus paymentStatus=PaymentStatus.READY;
        TossApprovalResult tossApprovalResult=null;
        String errorMessage=null;

        // 1. 락
        try {
//            aiServerClient.requestPaymentLock(carNumber);

            //결제 승인 요청 전 관리자 강제 출차 여부 확인
            SettlementRequestDto settlementRequestDto=SettlementRequestDto.builder().parkingLogId(parkingLog.getParkingLogId()).usedPoint((int)(parkingLog.getCalculatedFee()-dto.getAmount())).paidAmount((int)dto.getAmount()).build();
            settlementService.checkEligibility(settlementRequestDto,parkingLog);

            if (dto.getAmount() > 0) {
                // 2. 토스 승인 요청
                tossApprovalResult = tossPaymentService.confirmAndAnalyze(dto);
                if (tossApprovalResult.isSuccess()) {
                    paymentStatus = PaymentStatus.SUCCESS;
                } else {
                    paymentStatus = PaymentStatus.FAILED;
                    parkingLog.setPaymentRequestedAt(null);
                    errorMessage = tossApprovalResult.getErrorMessage();
                }
            } else {
                paymentStatus = PaymentStatus.SUCCESS;
            }
            // 3. db상태 변경
            return settlementService.processSettlementResult(parkingLog, payments, dto, paymentStatus, activityType, errorMessage);
        }catch (BusinessException e){
            //토스 승인 후에 관리자 강제 출차 내역이 있다면 환불 처리
            if(ErrorCode.FORCE_EXITED==e.getErrorCode() && tossApprovalResult!=null && tossApprovalResult.isSuccess()){
                refundService.refundByForceExit(String.valueOf(tossApprovalResult.getPaymentKey()), payments);
                return SettlementResponseDto.builder()
                        .paymentStatus(PaymentStatus.REFUNDED.toString())
                        .vehicleNumber(carNumber)
                        .paidAmount(0)
                        .message("이미 관리자에 의해 강제 출차 처리되었습니다.")
                        .build();
            }
            settlementService.restPaymentLock(payments,parkingLog);
            throw e;
        }catch(Exception e ){
            settlementService.restPaymentLock(payments,parkingLog);
            log.error("결제정보 처리 중 에러 :{}",e.getMessage());
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }finally {
            // 4. lock해제
            aiServerClient.requestPaymentLockRelease(carNumber);
        }
    }
}
