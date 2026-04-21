package com.example.demo.domain.payment.subscription.service;

import com.example.demo.domain.payment.Payment;
import com.example.demo.domain.payment.dtos.request.PaymentConfirmRequestDto;
import com.example.demo.domain.payment.enums.PaymentMethod;
import com.example.demo.domain.payment.enums.PaymentStatus;
import com.example.demo.domain.payment.enums.PaymentType;
import com.example.demo.domain.payment.point.entity.PointReason;
import com.example.demo.domain.payment.point.repository.UserPointRepository;
import com.example.demo.domain.payment.point.service.PointService;
import com.example.demo.domain.system.activitylog.ActivityLog;
import com.example.demo.domain.system.activitylog.enums.ActivityType;
import com.example.demo.domain.system.activitylog.repository.ActivityLogRepository;
import com.example.demo.domain.payment.repository.PaymentRepository;
import com.example.demo.domain.payment.service.TossPaymentService;
import com.example.demo.domain.payment.subscription.Subscription;
import com.example.demo.domain.payment.subscription.dtos.request.SubscriptionPurchaseRequestDto;
import com.example.demo.domain.payment.subscription.dtos.response.SubscriptionPolicyResponseDto;
import com.example.demo.domain.payment.subscription.dtos.response.SubscriptionRefundResponseDto;
import com.example.demo.domain.payment.subscription.dtos.response.SubscriptionResponseDto;
import com.example.demo.domain.payment.subscription.enums.Status;
import com.example.demo.domain.payment.subscription.repository.SubscriptionRepository;
import com.example.demo.domain.resident.User;
import com.example.demo.domain.resident.UserRepository;
import com.example.demo.domain.system.setting.SettingKey;
import com.example.demo.domain.system.setting.repository.SystemSettingRepository;
import com.example.demo.domain.vehicle.Vehicle;
import com.example.demo.domain.vehicle.VehicleRepository;
import com.example.demo.domain.vehicle.blacklist.repository.VehicleBlacklistRepository;
import com.example.demo.global.exception.BusinessException;
import com.example.demo.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 정기권 구매 및 환불 업무를 담당하는 서비스 레이어입니다.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final PaymentRepository paymentRepository;
    private final VehicleRepository vehicleRepository;
    private final VehicleBlacklistRepository vehicleBlacklistRepository;
    private final UserRepository userRepository;
    private final UserPointRepository userPointRepository;
    private final PointService pointService;
    private final TossPaymentService tossPaymentService;
    private final SystemSettingRepository systemSettingRepository;
    private final ActivityLogRepository activityLogRepository;

    /**
     * 정기권을 구매하고 결제 정보 및 포인트를 처리합니다.
     */
    public SubscriptionResponseDto purchase(Long userId, SubscriptionPurchaseRequestDto dto) {
        // 1. 유저 및 차량 존재 여부 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REQUEST));

        Vehicle vehicle = vehicleRepository.findById(dto.getVehicleId())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REQUEST));

        // 2. 차량 소유권 검증
        if (!vehicle.getUser().getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }

        // 3. 블랙리스트 여부 확인 (현재 시점 기준)
        if (vehicleBlacklistRepository.isCurrentlyBlacklisted(vehicle.getCarNumber(), LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }

        // 4. 해당 차량에 이미 사용 중인 정기권이 있는지 중복 체크
        if (subscriptionRepository.hasActiveSubscription(vehicle.getCarNumber(), LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }

        // 5. 시스템 설정값 로드 (가격, 기간 등)
        int subscriptionPrice = getSetting(SettingKey.SUBSCRIPTION_PRICE);
        int subscriptionDaysTemp = getSetting(SettingKey.SUBSCRIPTION_DAYS);

        // 6. 유저의 정기권 기간 중첩 여부 확인
        LocalDateTime purchaseStart = (dto.getStartDate() != null) ? dto.getStartDate() : LocalDateTime.now();
        LocalDateTime purchaseEnd   = purchaseStart.plusDays(subscriptionDaysTemp);
        if (subscriptionRepository.hasOverlappingSubscriptionForUser(userId, Status.ACTIVE, purchaseStart, purchaseEnd)) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }

        int subscriptionDays  = subscriptionDaysTemp;
        int minUsagePoint     = getSetting(SettingKey.MIN_USAGE_POINT);

        // 7. 포인트 사용 조건 검증 (최소 사용 포인트)
        if (dto.getUsedPoint() > 0 && dto.getUsedPoint() < minUsagePoint) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }

        // 8. 결제 금액 정합성 확인 (포인트 + 현금 == 정기권 가격)
        if (dto.getUsedPoint() + dto.getPaidAmount() != subscriptionPrice) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }

        // 9. PG사(토스) 결제 승인 요청
        if (dto.getPaidAmount() > 0) {
            PaymentConfirmRequestDto confirmDto = PaymentConfirmRequestDto.builder()
                    .paymentKey(dto.getPaymentKey())
                    .orderId(dto.getOrderId())
                    .amount(dto.getPaidAmount())
                    .parkingLogId(0L)
                    .build();
            var tossResult = tossPaymentService.confirmAndAnalyze(confirmDto);
            if (!tossResult.isSuccess()) {
                throw new BusinessException(ErrorCode.PG_PROVIDER_ERROR);
            }
        }

        // 10. 결제 수단별 Payment 생성
        // - 현금 결제분: Subscription FK로 사용 (cancel 로직에서 amount=현금액으로 포인트 역산)
        // - 포인트 결제분: 혼합 결제 시 추가 INSERT (이력 기록용)
        PaymentMethod mainMethod = (dto.getPaidAmount() > 0) ? PaymentMethod.PAY : PaymentMethod.POINT;
        Payment payment = Payment.builder()
                .vehicle(vehicle)
                .amount((long) dto.getPaidAmount())
                .priceSnapshot((long) subscriptionPrice)
                .paymentMethod(mainMethod)
                .paymentStatus(PaymentStatus.SUCCESS)
                .paymentType(PaymentType.SUBSCRIPTION)
                .externalPaymentId(dto.getOrderId())
                .paidAt(LocalDateTime.now())
                .build();
        paymentRepository.save(payment);

        Payment pointPayment = null;
        if (dto.getUsedPoint() > 0 && dto.getPaidAmount() > 0) {
            pointPayment = Payment.builder()
                    .vehicle(vehicle)
                    .amount((long) dto.getUsedPoint())
                    .priceSnapshot((long) subscriptionPrice)
                    .paymentMethod(PaymentMethod.POINT)
                    .paymentStatus(PaymentStatus.SUCCESS)
                    .paymentType(PaymentType.SUBSCRIPTION)
                    .paidAt(LocalDateTime.now())
                    .build();
            paymentRepository.save(pointPayment);
            // 환불 시 두 Payment 모두 조회할 수 있도록 역참조 키 저장
            pointPayment.setExternalPaymentId("POINT:" + payment.getPaymentId());
        }

        // 11. 사용한 포인트 차감 처리 (혼합 결제면 포인트 Payment ID 사용, 아니면 메인 ID)
        if (dto.getUsedPoint() > 0) {
            Long pointPaymentId = (pointPayment != null)
                    ? pointPayment.getPaymentId()
                    : payment.getPaymentId();
            pointService.usePoints(userId, pointPaymentId,
                    dto.getUsedPoint(), "정기권 구매 포인트 사용");
        }

        // 12. 정기권 정보 생성 및 저장
        LocalDateTime now   = LocalDateTime.now();
        LocalDateTime start = (dto.getStartDate() != null) ? dto.getStartDate() : now;
        Subscription subscription = new Subscription(
                null, user, vehicle,
                start, start.plusDays(subscriptionDays),
                Status.ACTIVE, payment,
                subscriptionPrice, now, now, null);
        subscriptionRepository.save(subscription);

        // 13. 포인트 적립 (순수 현금 결제인 경우에만 혜택 제공)
        int earnedPoint = 0;
        if (dto.getUsedPoint() == 0 && dto.getPaidAmount() > 0) {
            int earnRate = getSetting(SettingKey.PAYMENT_POINT_EARN_RATE);
            earnedPoint = dto.getPaidAmount() * earnRate / 100;
            pointService.earnPoints(userId, payment.getPaymentId(),
                    earnedPoint, "정기권 구매 포인트 적립");
        }

        activityLogRepository.save(ActivityLog.builder()
                .activityType(ActivityType.PASS_PURCHASED)
                .user(user)
                .payment(payment)
                .carNumber(vehicle.getCarNumber())
                .message("정기권 구매 완료")
                .build());

        return SubscriptionResponseDto.from(subscription, earnedPoint);
    }

    /**
     * 유저의 현재 보유 포인트를 조회합니다.
     */
    @Transactional(readOnly = true)
    public int getMyPoint(Long userId) {
        return userPointRepository.findByUserUserId(userId)
                .map(up -> up.getCurrentPoint())
                .orElse(0);
    }

    /**
     * 특정 시작일 기준, 정기권 구매 가능 잔여 수량을 조회합니다.
     */
    @Transactional(readOnly = true)
    public SubscriptionPolicyResponseDto getPolicy(LocalDateTime startDate) {
        int price    = getSetting(SettingKey.SUBSCRIPTION_PRICE);
        int days     = getSetting(SettingKey.SUBSCRIPTION_DAYS);
        int maxCount = getSetting(SettingKey.SUBSCRIPTION_MAX_COUNT);

        LocalDateTime start = (startDate != null) ? startDate : LocalDateTime.now();
        LocalDateTime end   = start.plusDays(days);

        // 해당 기간에 활성화된 전체 정기권 수량 체크
        long activeCount = subscriptionRepository.countOverlappingSubscriptions(Status.ACTIVE, start, end);
        long remaining   = Math.max(0, maxCount - activeCount);

        return SubscriptionPolicyResponseDto.builder()
                .price(price)
                .days(days)
                .maxCount(maxCount)
                .activeCount(activeCount)
                .remaining(remaining)
                .build();
    }

    /**
     * 정기권을 취소하고 남은 일수에 비례하여 금액 및 포인트를 환불합니다.
     */
    public SubscriptionRefundResponseDto cancel(Long userId, Long subscriptionId) {
        Subscription subscription = subscriptionRepository.findByIdWithVehicleAndPaymentForUpdate(subscriptionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REQUEST));

        // 1. 소유권 및 상태 검증 (본인 확인, 활성화 상태 여부, 기간 만료 여부)
        if (!subscription.getUser().getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }
        if (subscription.getStatus() != Status.ACTIVE) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }
        if (LocalDateTime.now().isAfter(subscription.getEndDate())) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }

        // 2. 남은 기간 비례 환불 비율 계산
        long totalDays = java.time.temporal.ChronoUnit.DAYS.between(subscription.getStartDate(), subscription.getEndDate());
        long remainDays = java.time.temporal.ChronoUnit.DAYS.between(LocalDateTime.now(), subscription.getEndDate());
        if (remainDays < 0) remainDays = 0;
        double ratio = totalDays > 0 ? (double) remainDays / totalDays : 0;

        Payment payment = subscription.getPayment();
        int paidAmount  = payment.getAmount().intValue();
        int totalPrice  = payment.getPriceSnapshot().intValue();
        int usedPoint   = totalPrice - paidAmount;

        // 혼합 결제 시 존재하는 포인트 Payment 조회 (역참조 키로 찾음)
        Payment pointPayment = paymentRepository
                .findFirstByExternalPaymentId("POINT:" + payment.getPaymentId())
                .orElse(null);

        // 3. [사전 계산] 순수 현금 결제인 경우 회수할 포인트와 잔고 부족분을 Toss 호출 전에 먼저 산출
        //    포인트 잔고가 부족하면 그 차액을 현금 환불액에서 공제해야 하므로 순서가 중요함
        int revokePoint = 0;
        int actualRevoke = 0;
        int shortfall = 0;
        if (usedPoint == 0 && paidAmount > 0) {
            int earnRate = getSetting(SettingKey.PAYMENT_POINT_EARN_RATE);
            int originalEarned = paidAmount * earnRate / 100;
            revokePoint = (int) (originalEarned * ratio);
            if (revokePoint > 0) {
                int currentBalance = userPointRepository.findByUserUserId(userId)
                        .map(up -> up.getCurrentPoint())
                        .orElse(0);
                actualRevoke = Math.min(revokePoint, currentBalance);
                shortfall = revokePoint - actualRevoke;
            }
        }

        // 4. 카드 결제 환불 (잔고 부족분만큼 공제 후 Toss 호출)
        int cashRefundAmount = 0;
        if (paidAmount > 0 && payment.getExternalPaymentId() != null) {
            int rawCashRefund = (int) (paidAmount * ratio);
            cashRefundAmount = Math.max(0, rawCashRefund - shortfall);
            if (cashRefundAmount > 0) {
                if (cashRefundAmount == paidAmount) {
                    tossPaymentService.cancelPayment(payment.getExternalPaymentId(), "정기권 환불");
                } else {
                    tossPaymentService.refundPayment(payment.getExternalPaymentId(), "정기권 부분환불", cashRefundAmount);
                }
            }
        }

        // 5. [포인트 반환] 결제 시 소모했던 포인트를 비율만큼 다시 적립
        int pointRefundAmount = 0;
        if (usedPoint > 0) {
            pointRefundAmount = (int) (usedPoint * ratio);
            if (pointRefundAmount > 0) {
                Long refPointPaymentId = (pointPayment != null)
                        ? pointPayment.getPaymentId()
                        : payment.getPaymentId();
                pointService.earnPoints(userId, refPointPaymentId,
                        pointRefundAmount, "정기권 환불 - 포인트 반환", PointReason.REFUND);
            }
        }

        // 6. [적립 포인트 회수] 잔고 내에서 회수 가능한 만큼만 차감 (부족분은 Step 4에서 현금 공제)
        if (actualRevoke > 0) {
            pointService.usePoints(userId, payment.getPaymentId(),
                    actualRevoke, "정기권 환불 - 적립 포인트 회수", PointReason.REFUND);
        }

        // 7. Payment 상태 업데이트 (메인 - 현금/포인트단독)
        PaymentStatus mainRefundStatus = (cashRefundAmount == paidAmount && paidAmount > 0) || (paidAmount == 0 && ratio >= 1.0)
                ? PaymentStatus.CANCELLED : PaymentStatus.REFUNDED;
        payment.setPaymentStatus(mainRefundStatus);
        payment.setRefundedAmount(payment.getRefundedAmount() + cashRefundAmount);

        // 8. Payment 상태 업데이트 (혼합 결제의 포인트 Payment)
        if (pointPayment != null) {
            PaymentStatus pointRefundStatus = (pointRefundAmount == usedPoint)
                    ? PaymentStatus.CANCELLED : PaymentStatus.REFUNDED;
            pointPayment.setPaymentStatus(pointRefundStatus);
            pointPayment.setRefundedAmount(pointPayment.getRefundedAmount() + pointRefundAmount);
        }

        // 9. 정기권 상태를 환불됨으로 변경
        subscription.updateStatus(Status.REFUNDED);

        activityLogRepository.save(ActivityLog.builder()
                .activityType(ActivityType.REFUNDED)
                .user(subscription.getUser())
                .payment(payment)
                .carNumber(subscription.getVehicle().getCarNumber())
                .message("정기권 환불 처리")
                .build());

        return SubscriptionRefundResponseDto.builder()
                .cashRefundAmount(cashRefundAmount)
                .pointRefundAmount(pointRefundAmount)
                .revokedPoint(actualRevoke)
                .pointDeductedAsCash(shortfall)
                .build();
    }

    /**
     * 유저의 전체 정기권 구매 이력을 조회합니다.
     */
    @Transactional(readOnly = true)
    public List<SubscriptionResponseDto> getMySubscriptions(Long userId) {
        return subscriptionRepository
                .findAllByUserIdOrderByEndDateDesc(userId)
                .stream()
                .map(s -> SubscriptionResponseDto.from(s, 0))
                .toList();
    }

    /**
     * 데이터베이스에서 시스템 설정값을 가져오거나 기본값을 반환합니다.
     */
    private int getSetting(SettingKey key) {
        return systemSettingRepository.findBySettingKey(key.getKey())
                .map(s -> Integer.parseInt(s.getSettingValue()))
                .orElse(key.getDefaultIntValue());
    }

}