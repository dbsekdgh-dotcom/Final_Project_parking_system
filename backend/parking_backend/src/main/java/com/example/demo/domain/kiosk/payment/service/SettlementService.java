package com.example.demo.domain.kiosk.payment.service;

import com.example.demo.domain.kiosk.exit.service.FreeExitRedisService;
import com.example.demo.domain.kiosk.payment.dtos.internal.AppliedTicketResult;
import com.example.demo.domain.kiosk.payment.dtos.request.PaymentConfirmRequestDto;
import com.example.demo.domain.kiosk.payment.dtos.request.SettlementRequestDto;
import com.example.demo.domain.kiosk.payment.dtos.response.FeeCalculationResponseDto;
import com.example.demo.domain.kiosk.payment.dtos.response.PaymentReadyResponseDto;
import com.example.demo.domain.kiosk.payment.dtos.response.SettlementResponseDto;
import com.example.demo.domain.kiosk.payment.dtos.response.VehiclePaymentResponseDto;
import com.example.demo.domain.shared.notification.Notification;
import com.example.demo.domain.shared.notification.enums.Status;
import com.example.demo.domain.shared.notification.enums.Type;
import com.example.demo.domain.shared.notification.repository.NotificationRepository;
import com.example.demo.domain.shared.activityLog.ActivityLog;
import com.example.demo.domain.shared.activityLog.enums.ActivityType;
import com.example.demo.domain.shared.activityLog.repository.ActivityLogRepository;
import com.example.demo.domain.shared.household.Household;
import com.example.demo.domain.shared.parkingTicket.ParkingTicket;
import com.example.demo.domain.shared.parkingTicket.repository.ParkingTicketRepository;
import com.example.demo.domain.shared.parkinglog.ParkingLog;
import com.example.demo.domain.shared.parkinglog.enums.ParkingStatus;
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
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
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
    private final EntityManager entityManager;
    private final ParkingTicketRepository parkingTicketRepository;
    private final FreeExitRedisService freeExitRedisService;

    //결제 전 검증
    public ParkingLog validateVehicleStatus(long parkingLogId) {
        ParkingLog parkingLog = parkingLogRepository.getDetailLogInfo(parkingLogId)
                .orElseThrow(() -> new BusinessException(ErrorCode.VEHICLE_NOT_ENTERED));

        //parkinglog null
        if(parkingLog==null) {
            throw new BusinessException(ErrorCode.VEHICLE_NOT_ENTERED);
        }
        //결제 대상자가 아닌 경우
        if(com.example.demo.domain.shared.parkinglog.enums.PaymentStatus.NONE.equals(parkingLog.getPaymentStatus())){
            return parkingLog;
        }
        // 관리자 강제 출차 확인
        if (ParkingStatus.FORCE_EXITED.equals(parkingLog.getParkingStatus())) {
            throw new BusinessException(ErrorCode.ALREADY_EXITED);
        }
        return parkingLog;
    }
    //before 사전 검증
    public void checkIfAlreadyProcessing(ParkingLog parkingLog,SettlementRequestDto settlementRequestDto){
        if (parkingLog.getPaymentRequestedAt() == null) {
            return;
        }
        // 시스템 설정 값: 결제 유효시간
        String paymentValidMinutes=systemSettingRepository.findBySettingKey(SettingKey.PAYMENT_VALID_MINUTES.getKey())
                .map(t->t.getSettingValue())
                .orElse(SettingKey.PAYMENT_VALID_MINUTES.name());
        int lockTimeOut=Integer.parseInt(paymentValidMinutes);
        boolean timeCheck=parkingLog.getPaymentRequestedAt().plusMinutes(lockTimeOut).isAfter(LocalDateTime.now());

        boolean isFree = (settlementRequestDto.getPaidAmount() + settlementRequestDto.getUsedPoint() == 0);
        // 결제 직후 바로 재조회 시
        if(isFree && parkingLog.getPaymentRequestedAt()!=null && com.example.demo.domain.shared.parkinglog.enums.PaymentStatus.PAID.equals(parkingLog.getPaymentStatus())) {
            throw new BusinessException(ErrorCode.ALREADY_PAID);
        }
        // 결제 후 시간이 지나서 조회하는 경우
        if(!isFree && parkingLog.getPaymentRequestedAt()!=null){
            // 결제 요청 시간이 찍힌 지 5분이 지났는지 확인
            if(timeCheck){
                throw new BusinessException(ErrorCode.ALREADY_PROCESSING);
            }
            log.info("결제 요청 시간이 만료되어 락을 무시하고 새로 진행합니다.");
        }
    }
    //after 사전 검증
    public void checkPaymentTimeout(ParkingLog parkingLog){
        // 시스템 설정 값: 결제 유효시간
        String paymentValidMinutes=systemSettingRepository.findBySettingKey(SettingKey.PAYMENT_VALID_MINUTES.getKey())
                .map(t->t.getSettingValue())
                .orElse(SettingKey.PAYMENT_VALID_MINUTES.name());
        int lockTimeOut=Integer.parseInt(paymentValidMinutes);
        //결제 요청 시간이 없으면 잘못된 접근
        if(parkingLog.getPaymentRequestedAt()==null){
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }
        //결제 요청 시간 검증(결제 시간 초과 에러)
        boolean timeCheck=parkingLog.getPaymentRequestedAt().plusMinutes(lockTimeOut).isAfter(LocalDateTime.now());
        if(!timeCheck){
            throw new BusinessException(ErrorCode.PAYMENT_TIMEOUT);
        }
    }
    
    //결제 전 검증
    public void checkEligibility(SettlementRequestDto settlementRequestDto,ParkingLog parkingLog){
        long parkingLogId=settlementRequestDto.getParkingLogId();
        int usedPoint=settlementRequestDto.getUsedPoint();
        int paidAmount= settlementRequestDto.getPaidAmount();
        long remainingFee = parkingLog.getCalculatedFee() - parkingLog.getFee(); // 전체 - 이미 낸 돈

        log.info("검증 실패 상세 - DB저장금액: {}, 프론트전달(카드+포인트): {}",
                remainingFee, (settlementRequestDto.getUsedPoint() + settlementRequestDto.getPaidAmount()));
        log.info("금액 검증 상세 분석 -> 목표: {}, 이번포인트: {}, 이번카드: {}",
                remainingFee, usedPoint, paidAmount);

        // 결제 금액 검증
        boolean amountCheck=remainingFee==(long)( usedPoint+ paidAmount);
        if(!amountCheck){
            throw new BusinessException(ErrorCode.INVALID_PAYMENT_AMOUNT);
        }
        //유저 포인트 검증
        User user=parkingLogRepository.getDetailLogInfo(parkingLogId).map(ParkingLog::getVehicle).map(Vehicle::getUser).orElse(null);
        int userCurrentPoint=user!=null?userPointRepository.findByUserUserId(user.getUserId()).get().getCurrentPoint() : 0;
        if(userCurrentPoint<usedPoint){
            throw new BusinessException(ErrorCode.INVALID_PAYMENT_AMOUNT);
        }
    }
    //사전검증 오류 발생 시
    public void releasePaymentLock(long parkingLogId){
        parkingLogRepository.findById(parkingLogId).ifPresent(parkingLog -> {
            parkingLog.setPaymentRequestedAt(null);
            parkingLogRepository.saveAndFlush(parkingLog);
            log.info("결제 락이 해제되었습니다. ID: {}", parkingLogId);
        });
    }

    // 결제 전 parkinglog update
    public ParkingLog updateParkingLogRequestedAt(ParkingLog parkingLog,VehiclePaymentResponseDto vehiclePaymentResponseDto){
        LocalDateTime now=LocalDateTime.now();
        FeeCalculationResponseDto FeeCalculationResponseDto= com.example.demo.domain.kiosk.payment.dtos.response.FeeCalculationResponseDto.builder()
                .rawFee(vehiclePaymentResponseDto.getRawFee())
                .calculatedFee(vehiclePaymentResponseDto.getCalculatedFee())
                .totalDiscountAmount(vehiclePaymentResponseDto.getTotalDiscountAmount())
                .totalDiscountMinutes(vehiclePaymentResponseDto.getTotalDiscountMinutes())
                .paymentRequestedAt(now)
                .build();
        parkingLog.requestPayment(FeeCalculationResponseDto);
        parkingLogRepository.saveAndFlush(parkingLog);
        entityManager.clear();

        // 할인권 사용 금액 업데이트
        if(vehiclePaymentResponseDto.getStackableTicketResult()!=null){
            updateUsedParkingTicket(vehiclePaymentResponseDto.getStackableTicketResult().getDetails());
        }

        // 깨끗해진 상태에서 DB의 진짜 최종본 읽어오기
        parkingLog = parkingLogRepository.getDetailLogInfo(parkingLog.getParkingLogId())
                .orElseThrow(() -> new BusinessException(ErrorCode.VEHICLE_NOT_ENTERED));
        return parkingLog;
    }

    //결제 전 payment insert
    public PaymentReadyResponseDto insertPayment(ParkingLog parkingLog, SettlementRequestDto settlementRequestDto, PaymentStatus paymentStatus, VehiclePaymentResponseDto vehiclePaymentResponseDto){

        Vehicle vehicle=parkingLog.getVehicle();
        User user=(vehicle!=null)?vehicle.getUser():null;
        String userEmail=(user!=null)? user.getEmail() : null;
        String tempPaymentId= UUID.randomUUID().toString();
        boolean isPaymentRequired=false;
        int paymentAmount=0;

        //- 포인트 결제금액이 있는 경우
        if(settlementRequestDto.getUsedPoint()>0){
            savePayment(parkingLog,vehicle,settlementRequestDto.getUsedPoint(),PaymentMethod.POINT,paymentStatus,tempPaymentId);
        }
        //- 신용카드 결제금액이 있는 경우
        if(settlementRequestDto.getPaidAmount()>0){
            savePayment(parkingLog,vehicle,settlementRequestDto.getPaidAmount(),PaymentMethod.PAY,paymentStatus,tempPaymentId);
            paymentAmount=settlementRequestDto.getPaidAmount();
            isPaymentRequired=true;

        }
        //- 무료
        if(settlementRequestDto.getPaidAmount()+ settlementRequestDto.getUsedPoint()==0){
            savePayment(parkingLog,vehicle,0L,PaymentMethod.FREE_POLICY,paymentStatus,tempPaymentId);
        }

        //4.리턴
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
        //결제금액이 0원이면 종료
        long totalPaidAmount = payments.stream()
                .mapToLong(Payment::getPriceSnapshot)
                .sum();
        if (totalPaidAmount == 0) {
            log.info("결제 금액이 0원이므로 포인트 로직을 건너뜁니다.");
            return;
        }
        //이미 처리된 요청이 있으면 종료
        Payment payment = payments.stream()
                .filter(p -> p.getPaymentMethod().equals(PaymentMethod.POINT)).findFirst().orElse(null);

        if(payment !=null){
            if(pointLogRepository.existsByPaymentPaymentId(payment.getPaymentId()))return;
        }

        String minUsagePoint=systemSettingRepository.findBySettingKey(SettingKey.MIN_USAGE_POINT.getKey())
                .map(SystemSetting::getSettingValue)
                .orElse(SettingKey.MIN_USAGE_POINT.name());

        // usedPoint= payment.getPriceSnapshot().intValue();
        int usedPoint=payments.stream()
                .filter(p->p.getPaymentMethod().equals(PaymentMethod.POINT))
                .mapToInt(p->p.getPriceSnapshot().intValue())
                .sum();
        int paidAmount=(int)paymentConfirmRequestDto.getAmount();

        if(Integer.parseInt(minUsagePoint)>usedPoint && usedPoint!=0){
            throw new BusinessException(ErrorCode.MINIMUM_POINT_NOT_ME);
        }

        String msg=null;
        //결제 시 포인트 사용 :  userPoint update& PointLog insert
        int changeAmount=usedPoint;
        PointReason pointReason=null;
        if(usedPoint>0){
            pointReason=PointReason.PAYMENT_USE;
            msg="결제 시 포인트 사용";
        }
        // 회원 && 결제 시 미사용 && 첫 적립
        String pointEarnRate=systemSettingRepository.findBySettingKey(SettingKey.PAYMENT_POINT_EARN_RATE.getKey())
                .map(SystemSetting::getSettingValue)
                .orElse(SettingKey.PAYMENT_POINT_EARN_RATE.name());
        if(usedPoint==0){
            pointReason=PointReason.PAYMENT_EARN;
            changeAmount=(int)Math.round(paidAmount*(Integer.parseInt(pointEarnRate)/100.0));
            payment=payments.stream().filter(p->p.getPaymentMethod().equals((PaymentMethod.PAY))).findFirst().orElse(null);
            msg="결제 포인트 적립";

        }
        if(payment==null){
            payment=payments.get(0);
        }

        updatePointOfPayment(user,changeAmount,payment,pointReason,msg);
    }

    public void updatePointOfPayment(User user,int changeAmount,Payment payment,PointReason pointReason,String msg){

        UserPoint userPoint=userPointRepository.findByUserUserId(user.getUserId()).orElse(null);
        long userId=user.getUserId();

        int updatedRow=0;
        int currentPoint=0;

        if(userPoint!=null){
            currentPoint=userPoint.getCurrentPoint();
            if(pointReason.isDeduction()){
                updatedRow=userPointRepository.decreasePoint(userId,changeAmount);
                //포인트 결제 실패한 경우
                if(updatedRow==0){
                    throw new BusinessException(ErrorCode.INSUFFICIENT_POINTS);
                }
            }else{
                userPointRepository.increasePoint(userId,changeAmount);
            }
        }else{
            //이전 내역이 없을 때
            UserPoint newUserPoint=UserPoint.builder().user(user).currentPoint(changeAmount).build();
            userPointRepository.save(newUserPoint);
        }

        //업데이트 후 최신 정보를 가져오기
        entityManager.refresh(userPoint);

        //pointLog insert
        PointLog pointLog=PointLog.builder()
                .payment(payment)
                .user(user)
                .changeAmount(changeAmount)
                .beforePoint(currentPoint)
                .afterPoint(userPoint.getCurrentPoint())
                .reason(pointReason)
                .description(msg)
                .build();
        pointLogRepository.save(pointLog);
    }

    //parking Log 업데이트/updateParkingLogFinal(fee ,payment_status ,paid_at,free_exit_until)
    public SettlementResponseDto updateParkingLogFinal(ParkingLog parkingLog,long paidAmount){
        String postPaymentGraceMinutes=systemSettingRepository.findBySettingKey(SettingKey.POST_PAYMENT_GRACE_MINUTES.getKey())
                .map(SystemSetting::getSettingValue)
                .orElse(SettingKey.POST_PAYMENT_GRACE_MINUTES.name());

        parkingLog.completePayment((int)paidAmount,Integer.parseInt(postPaymentGraceMinutes));
        DateTimeFormatter formatter=DateTimeFormatter.ofPattern("HH:mm");
        String deadlineStr=parkingLog.getFreeExitUntil().format(formatter);
        freeExitRedisService.register(parkingLog.getParkingLogId(),parkingLog.getFreeExitUntil());

        return SettlementResponseDto.builder()
                .paymentStatus(parkingLog.getPaymentStatus().name())
                .vehicleNumber(parkingLog.getCarNumberSnapshot())
                .paidAmount((int)paidAmount)
                .exitDeadline(deadlineStr)
                .message(String.format("정산이 완료되었습니다. %s까지 출차해 주세요.",deadlineStr))
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

    //할인권 사용 업데이트
    public void updateUsedParkingTicket(List<AppliedTicketResult> resultList){
        resultList.forEach(l->{
            parkingTicketRepository.findById(l.getTicketId()).ifPresent(ticket->{
                ticket.setAppliedAmount(l.getAppliedValue());
            });
        });
    }

    public SettlementResponseDto processSettlementResult(ParkingLog parkingLog,List<Payment> payments,PaymentConfirmRequestDto dto,PaymentStatus status,ActivityType activityType,String tossErrorMsg){
        SettlementResponseDto settlementResponseDto=null;

        // 결제 성공 시
        if (PaymentStatus.SUCCESS.equals(status)) {
            settlementResponseDto=handleSuccessSettlement(parkingLog,payments,dto,status,activityType);
        //결제 실패 시
        } else if (PaymentStatus.FAILED.equals(status)) {
            // - Payment insert
            savePaymentReceipt(payments, dto, status);
            // - 결제 요청 시간 삭제
            parkingLog.setPaymentRequestedAt(null);
            // - return
            settlementResponseDto=SettlementResponseDto.builder().paymentStatus(status.name()).vehicleNumber(parkingLog.getCarNumberSnapshot()).message(tossErrorMsg).build();
        //결제 취소 시
        } else if (PaymentStatus.CANCELLED.equals(status)) {
            // - Payment insert
            savePaymentReceipt(payments, dto, status);
            // - 결제 요청 시간 삭제
            parkingLog.setPaymentRequestedAt(null);
            // - return
            settlementResponseDto = SettlementResponseDto.builder().paymentStatus(status.name()).vehicleNumber(parkingLog.getCarNumberSnapshot()).build();
        }
        return settlementResponseDto;
    }

    public SettlementResponseDto handleSuccessSettlement(ParkingLog parkingLog,List<Payment> payments,PaymentConfirmRequestDto dto,PaymentStatus status,ActivityType activityType){
        SettlementResponseDto settlementResponseDto=null;
        User user=(parkingLog.getVehicle()!=null)? parkingLog.getVehicle().getUser():null;

        //DB의 최신 상태로 데이터 재조회
        entityManager.refresh(parkingLog);
        //토스 승인 후 관리자 강제 출차 여부 재확인
        if(ParkingStatus.FORCE_EXITED.equals(parkingLog.getParkingStatus())) {
            throw new BusinessException(ErrorCode.FORCE_EXITED);
        }
        // 1. Payment insert
        savePaymentReceipt(payments, dto, status);
        // 2. userPoint & PointLog update
        pointProcessOfPayment(user, parkingLog, payments, dto);
        // 3. parking Log 업데이트
        long totalAmount=payments.stream().mapToLong(Payment::getAmount).sum();
        settlementResponseDto = updateParkingLogFinal(parkingLog, totalAmount);
        // 4. notification insert
        insertNotification(user, settlementResponseDto.getExitDeadline());
        // 5. active log insert(포인트+카드 결제면 두줄?)// 사전정산인지, 출차 정산인지 여부는 컨트롤러에서
        insertActivityLog(parkingLog, user, payments, activityType);
          return  settlementResponseDto;
    }

    //락 해제
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void restPaymentLock(List<Payment> payments,ParkingLog parkingLog){
        payments.forEach(l->l.setPaymentStatus(PaymentStatus.FAILED));
        parkingLog.setPaymentRequestedAt(null);
        parkingLogRepository.saveAndFlush(parkingLog);
    }

}
