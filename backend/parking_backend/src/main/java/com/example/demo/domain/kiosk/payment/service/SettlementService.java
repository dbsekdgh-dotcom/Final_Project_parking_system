package com.example.demo.domain.kiosk.payment.service;

import com.example.demo.domain.kiosk.payment.dtos.request.SettlementRequestDto;
import com.example.demo.domain.kiosk.payment.dtos.response.PaymentReadyResponseDto;
import com.example.demo.domain.kiosk.payment.dtos.response.SettlementResponseDto;
import com.example.demo.domain.shared.Notification.Notification;
import com.example.demo.domain.shared.Notification.enums.Status;
import com.example.demo.domain.shared.Notification.enums.Type;
import com.example.demo.domain.shared.Notification.respository.NotificationRepository;
import com.example.demo.domain.shared.activityLog.ActivityLog;
import com.example.demo.domain.shared.activityLog.enums.ActivityType;
import com.example.demo.domain.shared.activityLog.respository.ActivityLogRepository;
import com.example.demo.domain.shared.household.Household;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class SettlementService {
    private final ParkingLogRepository parkingLogRepository;
    private final SystemSettingRepository systemSettingRepository;
    private final PaymentRepository paymentRepository;
    private final UserPointRepository userPointRepository;
    private final PointLogRepository pointLogRepository;
    private final NotificationRepository notificationRepository;
    private final ActivityLogRepository activityLogRepository;


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
        User user=parkingLogRepository.getDetailLogInfo(parkingLogId).map(i->i.getVehicle().getUser()).orElse(null);
        int userCurrentPoint=user!=null?userPointRepository.findByUserUserId(user.getUserId()).get().getCurrentPoint() : 0;
        if(userCurrentPoint<usedPoint){
            throw new BusinessException(ErrorCode.INVALID_PAYMENT_AMOUNT);
        }
        return  parkingLog;
    }
    //결제 전 payment insert
    public PaymentReadyResponseDto insertPayment(ParkingLog parkingLog, SettlementRequestDto settlementRequestDto, PaymentStatus paymentStatus){
        Vehicle vehicle=parkingLog.getVehicle();
        String tempPaymentId= UUID.randomUUID().toString();
        boolean isPaymentRequired=false;
        int paymentAmount=0;

        //포인트 결제금액이 있는 경우
        if(settlementRequestDto.getUsedPoint()>0){
            long p=savePayment(parkingLog,vehicle,settlementRequestDto.getUsedPoint(),PaymentMethod.POINT,paymentStatus,tempPaymentId);
        }
        //신용카드 결제금액이 있는 경우
        if(settlementRequestDto.getPaidAmount()>0){
            long p=savePayment(parkingLog,vehicle,settlementRequestDto.getPaidAmount(),PaymentMethod.PAY,paymentStatus,tempPaymentId);
            isPaymentRequired=true;

        }
        //무료
        if(settlementRequestDto.getPaidAmount()+ settlementRequestDto.getUsedPoint()==0){
            long p=savePayment(parkingLog,vehicle,0L,PaymentMethod.FREE_POLICY,paymentStatus,tempPaymentId);
        }
        //리턴
        return PaymentReadyResponseDto.builder().tempPaymentId(tempPaymentId).build();
    }

    public long savePayment(ParkingLog parkingLog, Vehicle vehicle,long priceSnapshot,PaymentMethod paymentMethod,PaymentStatus paymentStatus,String externalPaymentId){
        Payment payment=Payment.builder()
                .parkingLog(parkingLog)
                .vehicle(vehicle)
                .priceSnapshot((long)priceSnapshot)
                .paymentMethod(paymentMethod)
                .paymentStatus(paymentStatus)
                .externalPaymentId(externalPaymentId)
                .paymentType(PaymentType.PARKING).build();
        return paymentRepository.save(payment).getPaymentId();
    }
    //결제 후/결제 실패/결제 취소 시
    public void updatePaymentStatus(List<Long> paymentIds, PaymentStatus paymentstatus,long tossAmount){
        paymentIds.stream().forEach(p->{
            Payment payment=paymentRepository.findById(p)
                    .orElseThrow(()-> new BusinessException(ErrorCode.PAYMENT_NOT_COMPLETED));
            // 1. 결제 성공 시에 금액 업데이트
            if(paymentstatus==PaymentStatus.SUCCESS){
                if(payment.getPaymentMethod()==PaymentMethod.POINT){
                    payment.setAmount(payment.getPriceSnapshot());
                }else if(payment.getPaymentMethod()==PaymentMethod.PAY){
                    payment.setAmount(tossAmount);
                }else{
                    payment.setAmount(0L);
                }
            }
            // 2.상태 업데이트
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

    //결제 완료 알람
    public void insertNotification(User user,LocalDateTime freeExitUntil){
        if(user ==null) return;;
        Notification notification=Notification.builder().user(user).type(Type.PAYMENT).title("결제 완료 알람")
                .content("정산이 완료되었습니다. " +freeExitUntil.format(DateTimeFormatter.ofPattern("HH:mm")) +"까지 출차해 주세요.")
                .status(Status.ACTIVE).build();
        notificationRepository.save(notification);
    }

    //활동로그 기록
    public void insertActivityLog(ParkingLog parkingLog,User user, Payment payment, ActivityType activityType){
        Household household=null;
        if(user!=null){
            household=user.getHousehold();
        }

        ActivityLog activityLog=ActivityLog.builder()
                .activityType(activityType)
                .parkingLog(parkingLog)
                .payment(payment)
                .carNumber(parkingLog.getCarNumberSnapshot())
                .household(household)
                .message(activityType+"완료")
                .build();
        activityLogRepository.save(activityLog);
    }


}
