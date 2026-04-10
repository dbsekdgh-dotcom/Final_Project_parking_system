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
import com.example.demo.domain.kiosk.payment.service.TossPaymentService;
import com.example.demo.domain.shared.activityLog.enums.ActivityType;
import com.example.demo.domain.shared.parkinglog.ParkingLog;
import com.example.demo.domain.shared.parkinglog.repository.ParkingLogRepository;
import com.example.demo.domain.shared.payment.Payment;
import com.example.demo.domain.shared.payment.enums.PaymentMethod;
import com.example.demo.domain.shared.payment.enums.PaymentStatus;
import com.example.demo.domain.shared.payment.repository.PaymentRepository;
import com.example.demo.domain.shared.user.User;
import com.example.demo.domain.shared.vehicle.Vehicle;
import com.example.demo.global.exception.BusinessException;
import com.example.demo.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.springframework.http.ResponseEntity;
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

    public VehiclePaymentResponseDto paymentProcess(Long parkingLogID) {
        try {
            //1. 무료 대상인지 확인
            PaymentEligibilityResult result = paymentService.checkFreeExitEligibility(parkingLogID);
            ParkingLog parkingLog=result.getParkingLog();
            //2. 무료 대상이라면 리턴
            if (result.isFree()) {
                return result.getVehiclePaymentResponseDto();
            }
            //3. 락 걸기
            aiServerClient.requestPaymentLock(parkingLog.getCarNumberSnapshot());
            //4. 실제 요금정산 및 DB 작업 수행
            VehiclePaymentResponseDto responseDto= paymentService.requestPayment(parkingLog);
            if(responseDto.isFree()){
                //계산시 할인 등으로 인해 무료인 경우
                SettlementResponseDto settlementResponseDto= settlementService.updateParkingLogFinal(parkingLog,0);
                responseDto.setMessage(settlementResponseDto.getExitDeadline()+"까지 출차해주세요");
            }
            return responseDto;
        }catch (BusinessException e){
            e.printStackTrace();
            throw e;
        }catch (Exception e){
            log.error("정산 초기화 중 예상치 못한 에러: {}",e.getMessage());
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    //결제 요청 시 사전 검증
    public PaymentReadyResponseDto beforePayment(SettlementRequestDto dto){
        // 1. 결제 가능한지 검증(ex.결제 요청 시간부터 경과 시간, 결제 금액 변동 여부 확인)
        ParkingLog parkingLog=settlementService.checkEligibility(dto);
        // 2. 결제 전 Payment insert
        PaymentReadyResponseDto paymentReadyResponseDto=settlementService.insertPayment(parkingLog,dto, PaymentStatus.READY);
        // 3. 락 해제
        aiServerClient.requestPaymentLockRelease(parkingLog.getCarNumberSnapshot());
        return paymentReadyResponseDto;
    }

    //결제 성공/실패/취소 시
    public SettlementResponseDto afterPayment(PaymentConfirmRequestDto dto, ActivityType activityType){
        // 0. 기초정보 조회
        List<Payment> payments=paymentRepository.findByExternalPaymentId(dto.getOrderId());
        if(payments.isEmpty())throw new BusinessException(ErrorCode.INVALID_REQUEST);
        ParkingLog parkingLog=parkingLogRepository.findByParkingLogId(dto.getParkingLogId())
                .orElseThrow(()-> new BusinessException(ErrorCode.INVALID_REQUEST));
        String carNumber=parkingLog.getCarNumberSnapshot();
        PaymentStatus paymentStatus=PaymentStatus.READY;
        String tossErrorMsg=null;

        // 1. 락
        try {
            aiServerClient.requestPaymentLock(carNumber);
            log.info("락 완료 - carNumber: {}", carNumber);

            if(dto.getAmount()>0){
                // 2. 토스 승인 요청
                try{
                    log.info("결제 승인 요청 시작 - orderId: {}", dto.getOrderId());
                    ResponseEntity<JSONObject> tossResponse= tossPaymentService.confirmPayment(dto);
                    if(tossResponse.getStatusCode().is2xxSuccessful()){
                        paymentStatus=PaymentStatus.SUCCESS;
                    }else{
                        JSONObject body=tossResponse.getBody();
                        String code=(String) body.get("code");
                        if("ALREADY_PROCESSED_PAYMENT".equals(code)){
                            log.info("이미 처리된 결제건입니다. 성공으로 간주합니다.");
                            paymentStatus=PaymentStatus.SUCCESS;
                        }else{
                            tossErrorMsg=(String) body.get("message");
                            paymentStatus=PaymentStatus.FAILED;
                        }
                    }
                }catch (Exception e){
                    log.info("결제 통신 중 장애 발생 -  {}", e.getMessage());
                    paymentStatus=PaymentStatus.FAILED;
                    tossErrorMsg="결제 통신 중 장애가 발생하였습니다.";
                }
            }else{
                paymentStatus=PaymentStatus.SUCCESS;
            }

            // 3. db상태 변경
            log.info("DB 상태변경 시작  - parkingLogId: {}", dto.getParkingLogId());
            Vehicle vehicle=(parkingLog.getVehicle()!=null)?parkingLog.getVehicle():null;
            User user=(vehicle!=null)?vehicle.getUser():null;
            SettlementResponseDto settlementResponseDto=null;

            // 결제 성공 시
            if (PaymentStatus.SUCCESS.equals(paymentStatus)) {
                // 1. Payment insert
                settlementService.savePaymentReceipt(payments, dto, paymentStatus);
                // 2. userPoint & PointLog update
                settlementService.pointProcessOfPayment(user, parkingLog, payments, dto);
                // 3. parking Log 업데이트
                long totalAmount=payments.stream().mapToLong(Payment::getAmount).sum();
                settlementResponseDto = settlementService.updateParkingLogFinal(parkingLog, totalAmount);
                // 4. notification insert
                settlementService.insertNotification(user, settlementResponseDto.getExitDeadline());
                // 5. active log insert(포인트+카드 결제면 두줄?)// 사전정산인지, 출차 정산인지 여부는 컨트롤러에서
                settlementService.insertActivityLog(parkingLog, user, payments, activityType);
                //결제 실패 시
            } else if (PaymentStatus.FAILED.equals(paymentStatus)) {
                // - Payment insert
                settlementService.savePaymentReceipt(payments, dto, paymentStatus);
                // - 리액트에 에러 반환
                throw new BusinessException(tossErrorMsg,ErrorCode.PAYMENT_NOT_COMPLETED);
                //결제 취소 시
            } else if (PaymentStatus.CANCELLED.equals(paymentStatus)) {
                // - Payment insert
                settlementService.savePaymentReceipt(payments, dto, paymentStatus);
                // - return
                settlementResponseDto = SettlementResponseDto.builder()
                        .paymentStatus(paymentStatus.name())
                        .vehicleNumber(carNumber)
                        .build();
            }
            return settlementResponseDto;

        }catch (BusinessException e){
            throw e;
        }catch(Exception e){
            log.error("결제정보 처리 중 에러 :{}",e.getMessage());
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }finally {
            // 4. lock해제
            aiServerClient.requestPaymentLockRelease(carNumber);
        }
    }
}
