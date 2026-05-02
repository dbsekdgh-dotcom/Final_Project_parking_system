package com.example.demo.domain.payment.service;

import com.example.demo.domain.payment.dtos.internal.ParkingLogRefundDto;
import com.example.demo.domain.notification.Notification;
import com.example.demo.domain.notification.enums.Status;
import com.example.demo.domain.notification.enums.Type;
import com.example.demo.domain.notification.repository.NotificationRepository;
import com.example.demo.domain.system.activitylog.ActivityLog;
import com.example.demo.domain.system.activitylog.enums.ActivityType;
import com.example.demo.domain.system.activitylog.repository.ActivityLogRepository;
import com.example.demo.domain.resident.household.Household;
import com.example.demo.domain.parking.log.ParkingLog;
import com.example.demo.domain.parking.log.repository.ParkingLogRepository;
import com.example.demo.domain.payment.Payment;
import com.example.demo.domain.payment.enums.PaymentMethod;
import com.example.demo.domain.payment.enums.PaymentStatus;
import com.example.demo.domain.payment.enums.PaymentType;
import com.example.demo.domain.resident.User;
import com.example.demo.domain.payment.point.entity.PointLog;
import com.example.demo.domain.payment.point.entity.PointReason;
import com.example.demo.domain.payment.point.entity.UserPoint;
import com.example.demo.domain.payment.point.repository.PointLogRepository;
import com.example.demo.domain.payment.point.repository.UserPointRepository;
import com.example.demo.global.exception.BusinessException;
import com.example.demo.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefundService {
    private final TossPaymentService tossPaymentService;
    private final PointLogRepository pointLogRepository;
    private final UserPointRepository userPointRepository;
    private final NotificationRepository notificationRepository;
    private final ActivityLogRepository activityLogRepository;

    //강제 출차 시 환불
    @Transactional
    public void refundByForceExit(String paymentKey, List<Payment> payments){
        //1.toss 환불
        ResponseEntity<JSONObject> response=null;
        if(paymentKey!=null){
            try {
                response=tossPaymentService.cancelPayment(paymentKey,"중복 결제로 인한 환불");
            }catch (Exception e){
                //환불 실패 시, 시스템에서 자동으로 재실행 될 수 있도록/환불 실패하더라도 DB는 업데이트 되어야 함
                //환불 실패 로그 기록 및 관리자 알림
                log.error("Toss 환불 실패! 재시도 큐에 등록합니다.");
                // 비동기 재시도 처리: 실패한 환불 요청을 별도 테이블(PendingRefund)에 저장 후,Spring Batch나 @Scheduled를 통해 일정 주기로 재시도 로직 실행 필요
                // TODO: 스케줄러 기반 자동 재시도 로직 구현 필요
            }

        }
        //2.공통 사용 변수
        Payment creditPayment=payments.stream().filter(pament-> PaymentMethod.PAY.equals(pament.getPaymentMethod()))
                .findFirst().orElse(null);
        Payment pointPayment=payments.stream().filter(pament-> PaymentMethod.POINT.equals(pament.getPaymentMethod()))
                .findFirst().orElse(null);
        ParkingLog parkingLog=null;
        Payment payment=null;

        //3.환불 대상 정보 확정 및 금액 계산
        long creditRefundAmount=0L;
        if(creditPayment!=null){
            creditRefundAmount=creditPayment.getAmount();
            parkingLog=creditPayment.getParkingLog();
            payment=creditPayment;
        }
        long pointRestoreAmount=0L;
        if(pointPayment!=null){
            pointRestoreAmount=pointPayment.getAmount();
            if(parkingLog==null)pointPayment.getParkingLog();
            if(payment==null)payment=pointPayment;
        }
        long totalRefundAmount=creditRefundAmount+pointRestoreAmount;

        //4.payment 업데이트
        updatePaymentByRefund(payments,creditRefundAmount,pointRestoreAmount);

        //5.userPoint & PointLog update
        User user=parkingLog.getVehicle().getUser();
        pointLogUpdateByRefund(parkingLog,user,pointPayment,pointRestoreAmount);

        //6.parking_log update
        parkingLogUpdateByRefund(parkingLog,totalRefundAmount);

        //7.notification insert
        notificationUpdateByRefund(user,totalRefundAmount);

        //8.active log insert
        activeLogInsertByRefund(parkingLog,user,payment,creditRefundAmount,pointRestoreAmount);
    }

    // --- 아래는 단위 작업용 메서드들 ---
    private void updatePaymentByRefund(List<Payment> payments,long creditRefundedAmount, long pointRestoreAmount){
        payments.stream().filter(pament-> PaymentMethod.PAY.equals(pament.getPaymentMethod()))
                        .findFirst()
                        .ifPresent(p->{
                            if(p.getAmount()<creditRefundedAmount)throw new BusinessException(ErrorCode.INVALID_REFUND_AMOUNT);
                            p.setPaymentStatus(PaymentStatus.REFUNDED);
                            p.setRefundedAmount(creditRefundedAmount);
                        });
        payments.stream().filter(pament-> PaymentMethod.POINT.equals(pament.getPaymentMethod()))
                .findFirst()
                .ifPresent(p->{
                    if(p.getAmount()<pointRestoreAmount)throw new BusinessException(ErrorCode.INVALID_REFUND_AMOUNT);
                    p.setPaymentStatus(PaymentStatus.REFUNDED);
                    p.setRefundedAmount(pointRestoreAmount);
                });
    }
    private void pointLogUpdateByRefund(ParkingLog parkingLog,User user,Payment payment,long pointRestoreAmount){
        if(user==null || pointRestoreAmount==0)return;

        PointLog pointLog=pointLogRepository.findByPaymentPaymentId(payment.getPaymentId()).orElseThrow(()->new BusinessException(ErrorCode.INVALID_REQUEST));

        //userPoint 업데이트
        long userId=user.getUserId();
        UserPoint userPoint=userPointRepository.findByUserUserId(userId).orElseThrow(()->new BusinessException(ErrorCode.INVALID_REQUEST));
        long beforePoint=userPoint.getCurrentPoint();
        long afterPoint=beforePoint+pointRestoreAmount;
        userPoint.setCurrentPoint((int)afterPoint);

        //pointLog 업데이트
        if(pointLog.getChangeAmount()<pointRestoreAmount)throw new BusinessException(ErrorCode.INVALID_REFUND_AMOUNT);
        PointLog refundPointLog=PointLog.builder()
                .payment(payment).user(parkingLog.getVehicle().getUser()).changeAmount((int)pointRestoreAmount)
                .beforePoint((int)beforePoint).afterPoint((int)afterPoint).reason(PointReason.REFUND).build();
        pointLogRepository.save(refundPointLog);
    }
    private void parkingLogUpdateByRefund(ParkingLog parkingLog,long totalRefundedAmount){
        int fee=Math.max(0,parkingLog.getFee()-(int)totalRefundedAmount);
        boolean isFullRefund=fee==0;
        ParkingLogRefundDto dto=ParkingLogRefundDto.builder()
                .paymentStatus(com.example.demo.domain.parking.log.enums.PaymentStatus.REFUNDED)
                .fee(fee)
                .paidAt(isFullRefund? null:parkingLog.getPaidAt())
                .freeExitUntil(isFullRefund? null:parkingLog.getFreeExitUntil())
                .paymentRequestedAt(isFullRefund?null:parkingLog.getPaymentRequestedAt())
                .build();
        parkingLog.parkingLogRefund(dto);
    }
    private void notificationUpdateByRefund(User user,long totalRefundedAmount){
        if(user ==null) return;;
        String refundContent = String.format(
                "관리자 확인 및 강제 출차 처리에 따라 결제하신 주차 요금 %d원이 환불되었습니다.",
                totalRefundedAmount
        );
        Notification notification=Notification.builder().user(user).type(Type.REFUNDED).title("강제 출차로 인한 환불 안내")
                .content(refundContent)
                .status(Status.ACTIVE).build();
        notificationRepository.save(notification);
    }
    private void activeLogInsertByRefund(ParkingLog parkingLog,User user,Payment payment,long creditRefundedAmount, long pointRestoreAmount){
        Household household=(user !=null)? user.getHousehold() : null;

        ActivityLog activityLog=ActivityLog.builder().activityType(ActivityType.REFUNDED).parkingLog(parkingLog).payment(payment).carNumber(parkingLog.getCarNumberSnapshot())
                .household(household).message("카드: "+creditRefundedAmount+"원 취소 / 포인트: "+pointRestoreAmount+"원 복구 완료").build();
        activityLogRepository.save(activityLog);
    }
}
