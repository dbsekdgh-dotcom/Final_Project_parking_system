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
import com.example.demo.domain.shared.systemSetting.SettingKey;
import com.example.demo.domain.shared.systemSetting.SystemSetting;
import com.example.demo.domain.shared.systemSetting.repository.SystemSettingRepository;
import com.example.demo.domain.shared.ticketPolicy.enums.Status;
import com.example.demo.domain.shared.user.User;
import com.example.demo.domain.shared.vehicle.Vehicle;
import com.example.demo.domain.user.mypage.point.entity.PointLog;
import com.example.demo.domain.user.mypage.point.entity.PointReason;
import com.example.demo.domain.user.mypage.point.entity.UserPoint;
import com.example.demo.domain.user.mypage.point.repository.PointLogRepository;
import com.example.demo.domain.user.mypage.point.repository.UserPointRepository;
import com.example.demo.global.exception.BusinessException;
import com.example.demo.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Transactional
@RequiredArgsConstructor
public class SettlementService {
    private final ParkingLogRepository parkingLogRepository;
    private final SystemSettingRepository systemSettingRepository;
    private final PaymentRepository paymentRepository;
    private final UserPointRepository userPointRepository;
    private final PointLogRepository pointLogRepository;
    //무료/유료 정산 대상 업데이트 (검증 =>userpoint&pointLog => payment => parking_log(fee ,calculated_fee,payment_status ,paid_at,free_exit_until)=>알람)
    // 3. Payment insert/savePaymentReceipt
    // 2. userPoint & PointLog update/processPointDeduction

    // 4. parking Log 업데이트/updateParkingLogFinal
    // 5. Alram/sendSuccessAlarm
    //6.active

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
        String paymentValidMinutes=systemSettingRepository.findBySettingKey(SettingKey.PAYMENT_VALID_MINUTES.getKey())
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
    public List<Long> insertPayment(ParkingLog parkingLog, SettlementRequestDto settlementRequestDto, PaymentStatus paymentStatus,boolean isPaymentSuccess){
        int amount=0;
        List<Long> paymentIds=new ArrayList<>();
        Vehicle vehicle=parkingLog.getVehicle();
        //포인트 결제금액이 있는 경우
        if(settlementRequestDto.getUsedPoint()>0){
            if(isPaymentSuccess)amount=settlementRequestDto.getUsedPoint();
            long p=savePayment(parkingLog,vehicle,amount,settlementRequestDto.getUsedPoint(),PaymentMethod.POINT,paymentStatus);
            paymentIds.add(p);
        }
        //신용카드 결제금액이 있는 경우
        if(settlementRequestDto.getPaidAmount()>0){
            if(isPaymentSuccess)amount=settlementRequestDto.getPaidAmount();
            long p=savePayment(parkingLog,vehicle,amount,settlementRequestDto.getPaidAmount(),PaymentMethod.PAY,paymentStatus);
            paymentIds.add(p);
        }
        //무료
        if(settlementRequestDto.getPaidAmount()+ settlementRequestDto.getUsedPoint()==0){
            long p=savePayment(parkingLog,vehicle,0L,0L,PaymentMethod.FREE_POLICY,paymentStatus);
            paymentIds.add(p);
        }
        return paymentIds;
    }

    public long savePayment(ParkingLog parkingLog, Vehicle vehicle,long amount,long priceSnapshot,PaymentMethod paymentMethod,PaymentStatus paymentStatus){
        Payment payment=Payment.builder()
                .parkingLog(parkingLog)
                .vehicle(vehicle)
                .amount(amount)
                .priceSnapshot((long)priceSnapshot)
                .paymentMethod(paymentMethod)
                .paymentStatus(paymentStatus)
                .paymentType(PaymentType.PARKING).build();
        return paymentRepository.save(payment).getPaymentId();
    }
    //결제 후/결제 실패/결제 취소 시
    public void updatePaymentStatus(List<Long> paymentIds, PaymentStatus paymentstatus){
        paymentIds.stream().forEach(p->{
            Payment payment=paymentRepository.findById(p).orElse(null);
            if(payment==null){
                throw new BusinessException(ErrorCode.PAYMENT_NOT_COMPLETED);
            }
            payment.setPaymentStatus(paymentstatus);
        });
    }

    //
    public void pointProcessOfPayment(ParkingLog parkingLog,SettlementRequestDto settlementRequestDto,Payment payment){
        String minUsagePoint=systemSettingRepository.findBySettingKey(SettingKey.MIN_USAGE_POINT.getKey())
                .map(SystemSetting::getSettingValue)
                .orElse("100");

        int usedPoint=settlementRequestDto.getUsedPoint();
        if(Integer.parseInt(minUsagePoint)>usedPoint && usedPoint!=0){
            throw new BusinessException(ErrorCode.MINIMUM_POINT_NOT_ME);
        }

        //회원인지 아닌지 구분
        User user=parkingLog.getVehicle().getUser();
        if(user==null){
            return;
        }
        //결제 시 포인트 사용 :  userPoint update& PointLog insert
        int changeAmount=usedPoint;
        PointReason pointReason=null;
        if(usedPoint>0){
            pointReason=PointReason.PAYMENT_USE;
        }
        // 회원 && 결제 시 미사용 && 첫 적립
        String pointEarnRate=systemSettingRepository.findBySettingKey(SettingKey.PAYMENT_POINT_EARN_RATE.getKey())
                .map(SystemSetting::getSettingValue)
                .orElse("1");
        if(usedPoint==0){
            pointReason=PointReason.PAYMENT_EARN;
            changeAmount=(int)Math.round(settlementRequestDto.getPaidAmount()*(Integer.parseInt(pointEarnRate)/100.0));
        }
        updatePointOfPayment(user,changeAmount,payment,pointReason);
    }

    public void updatePointOfPayment(User user,int changeAmount,Payment payment,PointReason pointReason){

        UserPoint userPoint=userPointRepository.findByUserUserId(user.getUserId()).orElse(null);

        int currentPoint=0;
        if(userPoint!=null){
            currentPoint=userPoint.getCurrentPoint();
        }
        int afterPoint=0;

        if(pointReason.isDeduction()){
            //결제 시 사용, 환불, 관리자 회수인 경우
            afterPoint=currentPoint- changeAmount;
        }else{
            //결제 시 미사용, 관리자 지급인 경우
            afterPoint=currentPoint+changeAmount;
        }
        if(userPoint!=null){
            //이전 내역이 있을 때
            userPoint.setCurrentPoint(afterPoint);
        }else{
            //이전 내역이 없을 때
            UserPoint newUserPoint=UserPoint.builder().user(user).currentPoint(afterPoint).build();
            userPointRepository.save(newUserPoint);
        }

        //pointLog insert
        PointLog pointLog=PointLog.builder()
                .payment(payment)
                .user(user)
                .changeAmount(changeAmount)
                .beforePoint(currentPoint)
                .afterPoint(afterPoint)
                .reason(pointReason)
                .build();
        pointLogRepository.save(pointLog);
    }

    //parking Log 업데이트/updateParkingLogFinal(fee ,payment_status ,paid_at,free_exit_until)
    public SettlementResponseDto updateParkingLogFinal(ParkingLog parkingLog,int paidAmount){
        String postPaymentGraceMinutes=systemSettingRepository.findBySettingKey(SettingKey.POST_PAYMENT_GRACE_MINUTES.getKey())
                .map(SystemSetting::getSettingValue)
                .orElse("5");
        LocalDateTime nowtime=LocalDateTime.now();
        LocalDateTime freeExitUntil=nowtime.plusMinutes(Integer.parseInt(postPaymentGraceMinutes));
        int finalPaidAmount=parkingLog.getFee()+paidAmount;
        com.example.demo.domain.shared.parkinglog.enums.PaymentStatus paymentStatus= com.example.demo.domain.shared.parkinglog.enums.PaymentStatus.PAID;
        parkingLog.setFee(finalPaidAmount);
        parkingLog.setPaidAt(nowtime);
        parkingLog.setFreeExitUntil(freeExitUntil);
        parkingLog.setPaymentStatus(paymentStatus);

        DateTimeFormatter formatter=DateTimeFormatter.ofPattern("HH:mm:ss");
        return SettlementResponseDto.builder()
                .paymentStatus(paymentStatus.name())
                .vehicleNumber(parkingLog.getCarNumberSnapshot())
                .paidAmount(paidAmount)
                .exitDeadline(freeExitUntil.format(formatter).formatted())
                .build();
    }

}
