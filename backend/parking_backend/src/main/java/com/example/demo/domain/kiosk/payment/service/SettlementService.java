package com.example.demo.domain.kiosk.payment.service;

import com.example.demo.domain.kiosk.payment.dtos.request.SettlementRequestDto;
import com.example.demo.domain.kiosk.payment.dtos.response.SettlementResponseDto;
import com.example.demo.domain.shared.parkinglog.ParkingLog;
import com.example.demo.domain.shared.parkinglog.repository.ParkingLogRepository;
import com.example.demo.domain.shared.payment.Payment;
import com.example.demo.domain.shared.payment.enums.PaymentMethod;
import com.example.demo.domain.shared.payment.enums.PaymentStatus;
import com.example.demo.domain.shared.payment.enums.PaymentType;
import com.example.demo.domain.shared.payment.repository.PaymentRepository;
import com.example.demo.domain.shared.systemSetting.repository.SystemSettingRepository;
import com.example.demo.domain.shared.ticketPolicy.enums.Status;
import com.example.demo.domain.shared.vehicle.Vehicle;
import com.example.demo.global.exception.BusinessException;
import com.example.demo.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Transactional
@RequiredArgsConstructor
public class SettlementService {
    private final ParkingLogRepository parkingLogRepository;
    private final SystemSettingRepository systemSettingRepository;
    private final PaymentRepository paymentRepository;
    //무료/유료 정산 대상 업데이트 (검증 =>userpoint&pointLog => payment => parking_log(fee ,calculated_fee,payment_status ,paid_at,free_exit_until)=>알람)
    // 3. Payment insert/savePaymentReceipt
    // 2. userPoint & PointLog update/processPointDeduction

    // 4. parking Log 업데이트/updateParkingLogFinal
    // 5. Alram/sendSuccessAlarm
    //포인트 적립??

    //결제 전 검증
    public ParkingLog checkEligibility(SettlementRequestDto settlementRequestDto){
        long parkingLogId=settlementRequestDto.getParkingLogId();
        int usedPoint=settlementRequestDto.getUsedPoint();
        int paidAmount= settlementRequestDto.getPaidAmount();
        //결제 금액 검증
        ParkingLog parkingLog=parkingLogRepository.findByParkingLogId(parkingLogId).orElse(null);
        if(parkingLog==null){
            throw new BusinessException(ErrorCode.VEHICLE_NOT_ENTERED);
        }
        int prepaid=parkingLog.getFee()!=0?parkingLog.getFee():0;
        boolean amountCheck=parkingLog.getCalculatedFee()==prepaid+ usedPoint+ paidAmount;
        if(!amountCheck){
            throw new BusinessException(ErrorCode.PAYMENT_TIMEOUT);
        }
        //결제 요청 시간 검증
        String paymentValidMinutes=systemSettingRepository.findBySettingKey("PAYMENT_VALID_MINUTES")
                .map(t->t.getSettingValue())
                .orElse("5");
        boolean timeCheck=parkingLog.getPaymentRequestedAt().plusMinutes(Integer.parseInt(paymentValidMinutes)).isAfter(LocalDateTime.now());
        if(!timeCheck){
            throw new BusinessException(ErrorCode.PAYMENT_TIMEOUT);
        }
        //유저 포인트 검증
        int userCurrentPoint=parkingLogRepository.getDetailLogInfo(parkingLogId).map(i->i.getCurrentPoint()).orElse(0);
        if(userCurrentPoint<usedPoint){
            throw new BusinessException(ErrorCode.INVALID_PAYMENT_AMOUNT);
        }
        return  parkingLog;
    }
    //결제 전 payment insert
    public void insertPayment(ParkingLog parkingLog, SettlementRequestDto settlementRequestDto, PaymentStatus paymentStatus,long amount){
        Vehicle vehicle=parkingLog.getVehicle();
        //포인트 결제금액이 있는 경우
        if(settlementRequestDto.getUsedPoint()>0){
            savePayment(parkingLog,vehicle,amount,settlementRequestDto.getUsedPoint(),PaymentMethod.POINT,paymentStatus);
        }
        //신용카드 결제금액이 있는 경우
        if(settlementRequestDto.getPaidAmount()>0){
            savePayment(parkingLog,vehicle,amount,settlementRequestDto.getPaidAmount(),PaymentMethod.PAY,paymentStatus);
        }
        //무료
        if(settlementRequestDto.getPaidAmount()+ settlementRequestDto.getUsedPoint()==0){
            savePayment(parkingLog,vehicle,amount,0L,PaymentMethod.FREE_POLICY,paymentStatus);
        }
    }

    public Payment savePayment(ParkingLog parkingLog, Vehicle vehicle,long amount,long priceSnapshot,PaymentMethod paymentMethod,PaymentStatus paymentStatus){
        Payment payment=Payment.builder()
                .parkingLog(parkingLog)
                .vehicle(vehicle)
                .amount(amount)
                .priceSnapshot((long)priceSnapshot)
                .paymentMethod(paymentMethod)
                .paymentStatus(paymentStatus)
                .paymentType(PaymentType.PARKING).build();
        return paymentRepository.save(payment);
    }
    //결제 후/결제 실패/결제 취소 시
    public void updatePaymentStatus(long paymentId, PaymentStatus paymentstatus){
        Payment payment=paymentRepository.findById(paymentId).orElse(null);
        if(payment==null){
            throw new BusinessException(ErrorCode.PAYMENT_NOT_COMPLETED);
        }
        payment.setPaymentStatus(paymentstatus);
    }

    //userPoint & PointLog update/processPointDeduction
    public void processPointDeduction(){

    }

}
