package com.example.demo.domain.kiosk.payment.service;

import com.example.demo.domain.kiosk.payment.dtos.request.PaymentConfirmRequestDto;
import com.example.demo.domain.kiosk.payment.dtos.request.SettlementRequestDto;
import com.example.demo.domain.kiosk.payment.dtos.response.PaymentReadyResponseDto;
import com.example.demo.domain.kiosk.payment.dtos.response.SettlementResponseDto;
import com.example.demo.domain.shared.Notification.Notification;
import com.example.demo.domain.shared.Notification.enums.Status;
import com.example.demo.domain.shared.Notification.enums.Type;
import com.example.demo.domain.shared.Notification.respository.NotificationRepository;
import com.example.demo.domain.shared.activityLog.ActivityLog;
import com.example.demo.domain.shared.activityLog.enums.ActivityType;
import com.example.demo.domain.shared.activityLog.repository.ActivityLogRepository;
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
        if(com.example.demo.domain.shared.parkinglog.enums.PaymentStatus.NONE.equals(parkingLog.getPaymentStatus())){
            return parkingLog;
        }
        //혹시 요금 무료 대상자가 넘어오는 경우 방지
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
        User user=parkingLogRepository.getDetailLogInfo(parkingLogId).map(ParkingLog::getVehicle).map(Vehicle::getUser).orElse(null);
        int userCurrentPoint=user!=null?userPointRepository.findByUserUserId(user.getUserId()).get().getCurrentPoint() : 0;
        if(userCurrentPoint<usedPoint){
            throw new BusinessException(ErrorCode.INVALID_PAYMENT_AMOUNT);
        }
        return  parkingLog;
    }
    //결제 전 payment insert
    public PaymentReadyResponseDto insertPayment(ParkingLog parkingLog, SettlementRequestDto settlementRequestDto, PaymentStatus paymentStatus){
        Vehicle vehicle=parkingLog.getVehicle();
        User user=(vehicle!=null)?vehicle.getUser():null;
        String userEmail=(user!=null)? user.getEmail() : null;
        String tempPaymentId= UUID.randomUUID().toString();
        boolean isPaymentRequired=false;
        int paymentAmount=0;

        //포인트 결제금액이 있는 경우
        if(settlementRequestDto.getUsedPoint()>0){
            savePayment(parkingLog,vehicle,settlementRequestDto.getUsedPoint(),PaymentMethod.POINT,paymentStatus,tempPaymentId);
        }
        //신용카드 결제금액이 있는 경우
        if(settlementRequestDto.getPaidAmount()>0){
            savePayment(parkingLog,vehicle,settlementRequestDto.getPaidAmount(),PaymentMethod.PAY,paymentStatus,tempPaymentId);
            paymentAmount=settlementRequestDto.getPaidAmount();
            isPaymentRequired=true;

        }
        //무료
        if(settlementRequestDto.getPaidAmount()+ settlementRequestDto.getUsedPoint()==0){
            savePayment(parkingLog,vehicle,0L,PaymentMethod.FREE_POLICY,paymentStatus,tempPaymentId);
        }

        //리턴
        return PaymentReadyResponseDto.builder()
                .orderId(tempPaymentId)
                .orderName(String.format("[%s] 주차 요금 정산",parkingLog.getCarNumberSnapshot()))
                .isPaymentRequired(isPaymentRequired)
                .parkingLogId(parkingLog.getParkingLogId())
                .vehicleNumber(parkingLog.getCarNumberSnapshot())
                .userEmail(userEmail)
                .amount(paymentAmount)
                .build();
    }

    public void savePayment(ParkingLog parkingLog, Vehicle vehicle,long priceSnapshot,PaymentMethod paymentMethod,PaymentStatus paymentStatus,String externalPaymentId){
        Payment payment=Payment.builder()
                .parkingLog(parkingLog)
                .vehicle(vehicle)
                .priceSnapshot((long)priceSnapshot)
                .paymentMethod(paymentMethod)
                .paymentStatus(paymentStatus)
                .externalPaymentId(externalPaymentId)
                .paymentType(PaymentType.PARKING).build();
        paymentRepository.save(payment).getPaymentId();
    }
    //결제 후/결제 실패/결제 취소 시
    public void savePaymentReceipt(List<Payment> payments,PaymentConfirmRequestDto paymentConfirmRequestDto,PaymentStatus paymentStatus){
        payments.forEach(payment->{
            // 1. 결제 성공 시에 금액 업데이트
            if(paymentStatus==PaymentStatus.SUCCESS){
                if(payment.getPaymentMethod()==PaymentMethod.POINT){
                    payment.setAmount(payment.getPriceSnapshot());
                }else if(payment.getPaymentMethod()==PaymentMethod.PAY){
                    payment.setAmount(paymentConfirmRequestDto.getAmount());
                }else{
                    payment.setAmount(0L);
                }
                payment.setExternalPaymentId(paymentConfirmRequestDto.getPaymentKey());
            }else {
                payment.setAmount(0L);
            }
            // 2.상태 업데이트
            payment.setPaidAt(LocalDateTime.now());
            payment.setPaymentStatus(paymentStatus);
        });
    }

    //포인트 업데이트
    public void pointProcessOfPayment(User user,ParkingLog parkingLog,List<Payment> payments,PaymentConfirmRequestDto paymentConfirmRequestDto){
        //회원인지 아닌지 구분
        if(user==null){
            return;
        }
        //이미 처리된 요청이 있으면 종료
        Payment payment=payments.stream()
                .filter(p->p.getPaymentMethod().equals(PaymentMethod.POINT)).findFirst().orElse(null);

        if(payment !=null){
            if(pointLogRepository.existsByPaymentPaymentId(payment.getPaymentId()))return;
        }

        String minUsagePoint=systemSettingRepository.findBySettingKey(SettingKey.MIN_USAGE_POINT.getKey())
                .map(SystemSetting::getSettingValue)
                .orElse("100");

        // usedPoint= payment.getPriceSnapshot().intValue();
        int usedPoint=payments.stream()
                .filter(p->p.getPaymentMethod().equals(PaymentMethod.POINT))
                .mapToInt(p->p.getPriceSnapshot().intValue())
                .sum();
        int paidAmount=(int)paymentConfirmRequestDto.getAmount();

        if(Integer.parseInt(minUsagePoint)>usedPoint && usedPoint!=0){
            throw new BusinessException(ErrorCode.MINIMUM_POINT_NOT_ME);
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
            changeAmount=(int)Math.round(paidAmount*(Integer.parseInt(pointEarnRate)/100.0));
            payment=payments.stream().filter(p->p.getPaymentMethod().equals((PaymentMethod.PAY))).findFirst().orElse(null);
        }
        if(payment==null){
            payment=payments.get(0);
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
    public SettlementResponseDto updateParkingLogFinal(ParkingLog parkingLog,long paidAmount){
        String postPaymentGraceMinutes=systemSettingRepository.findBySettingKey(SettingKey.POST_PAYMENT_GRACE_MINUTES.getKey())
                .map(SystemSetting::getSettingValue)
                .orElse("5");
        LocalDateTime nowtime=LocalDateTime.now();
        LocalDateTime freeExitUntil=nowtime.plusMinutes(Integer.parseInt(postPaymentGraceMinutes));
        int finalPaidAmount= parkingLog.getFee()+(int)paidAmount;
        com.example.demo.domain.shared.parkinglog.enums.PaymentStatus paymentStatus= com.example.demo.domain.shared.parkinglog.enums.PaymentStatus.PAID;
        parkingLog.setFee(finalPaidAmount);
        parkingLog.setPaidAt(nowtime);
        parkingLog.setFreeExitUntil(freeExitUntil);
        parkingLog.setPaymentStatus(paymentStatus);

        DateTimeFormatter formatter=DateTimeFormatter.ofPattern("HH:mm");
        return SettlementResponseDto.builder()
                .paymentStatus(paymentStatus.name())
                .vehicleNumber(parkingLog.getCarNumberSnapshot())
                .paidAmount((int)paidAmount)
                .exitDeadline(freeExitUntil.format(formatter).formatted())
                .message("정산이 완료되었습니다. " +freeExitUntil.format(formatter).formatted() +"까지 출차해 주세요.")
                .build();
    }

    //결제 완료 알람
    public void insertNotification(User user,String freeExitUntil){
        if(user ==null) return;;
        Notification notification=Notification.builder().user(user).type(Type.PAYMENT).title("결제 완료 알람")
                .content("정산이 완료되었습니다. " +freeExitUntil +"까지 출차해 주세요.")
                .status(Status.ACTIVE).build();
        notificationRepository.save(notification);
    }

    //활동로그 기록
    public void insertActivityLog(ParkingLog parkingLog,User user, List<Payment> payments, ActivityType activityType){
        Household household=(user !=null)? user.getHousehold() : null;

        payments.forEach(payment->{
            ActivityLog activityLog=ActivityLog.builder()
                    .activityType(activityType)
                    .parkingLog(parkingLog)
                    .payment(payment)
                    .carNumber(parkingLog.getCarNumberSnapshot())
                    .household(household)
                    .message(activityType+"완료")
                    .build();
            activityLogRepository.save(activityLog);
        });
    }
}
