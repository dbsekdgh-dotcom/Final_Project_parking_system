package com.example.demo.domain.payment.subscription.service;

import com.example.demo.domain.payment.Payment;
import com.example.demo.domain.payment.dtos.internal.TossApprovalResult;
import com.example.demo.domain.payment.dtos.request.PaymentConfirmRequestDto;
import com.example.demo.domain.payment.enums.PaymentMethod;
import com.example.demo.domain.payment.enums.PaymentStatus;
import com.example.demo.domain.payment.enums.PaymentType;
import com.example.demo.domain.payment.repository.PaymentRepository;
import com.example.demo.domain.payment.service.TossPaymentService;
import com.example.demo.domain.system.activitylog.ActivityLog;
import com.example.demo.domain.system.activitylog.enums.ActivityType;
import com.example.demo.domain.system.activitylog.repository.ActivityLogRepository;
import com.example.demo.domain.payment.subscription.Subscription;
import com.example.demo.domain.payment.subscription.dtos.request.SubscriptionConfirmRequestDto;
import com.example.demo.domain.payment.subscription.dtos.request.SubscriptionReadyRequestDto;
import com.example.demo.domain.payment.subscription.dtos.response.SubscriptionHistoryResponseDto;
import com.example.demo.domain.payment.subscription.dtos.response.SubscriptionPolicyResponseDto;
import com.example.demo.domain.payment.subscription.dtos.response.SubscriptionMyInfoResponseDto;
import com.example.demo.domain.payment.subscription.dtos.response.SubscriptionReadyResponseDto;
import com.example.demo.domain.payment.subscription.dtos.response.SubscriptionRefundResponseDto;
import com.example.demo.domain.payment.subscription.enums.Status;
import com.example.demo.domain.payment.subscription.repository.SubscriptionRepository;
import com.example.demo.domain.resident.User;
import com.example.demo.domain.resident.UserRepository;
import com.example.demo.domain.system.setting.SystemSetting;
import com.example.demo.domain.system.setting.repository.SystemSettingRepository;
import com.example.demo.domain.vehicle.Vehicle;
import com.example.demo.domain.vehicle.VehicleRepository;
import com.example.demo.domain.vehicle.enums.VehicleStatus;
import com.example.demo.global.exception.CustomException;
import com.example.demo.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final SystemSettingRepository systemSettingRepository;
    private final VehicleRepository vehicleRepository;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final TossPaymentService tossPaymentService;
    private final ActivityLogRepository activityLogRepository;


    @Transactional
    public SubscriptionReadyResponseDto prepareSubscription(Long userId, SubscriptionReadyRequestDto requestDto) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Vehicle vehicle = vehicleRepository.findByCarNumber(requestDto.getCarNumber())
                .orElseThrow(() -> new CustomException(ErrorCode.VEHICLE_NOT_FOUND));

        if (vehicle.getUser() == null || !vehicle.getUser().getUserId().equals(userId)) {
            throw new CustomException(ErrorCode.ACCESS_DENIED_VEHICLE);
        }

        // 차량이 승인된 ACTIVE 상태가 아니면 정기권 구매 불가 (PENDING, DELETED 등)
        if (vehicle.getStatus() != VehicleStatus.ACTIVE) {
            throw new CustomException(ErrorCode.INVALID_VEHICLE_STATUS);
        }

        // 시작일이 오늘보다 이전이면 불가 — 과거 날짜로 정기권 구매 방지
        if (requestDto.getStartDate().isBefore(LocalDateTime.now())) {
            throw new CustomException(ErrorCode.INVALID_SUBSCRIPTION_PERIOD);
        }

        SystemSetting maxCountSettiong = systemSettingRepository.findByIdWithLock("SUB_MAX_COUNT")
                .orElseThrow(() -> new CustomException(ErrorCode.SYSTEM_SETTING_NOT_FOUND));

        SystemSetting priceSettiong = systemSettingRepository.findBySettingKey("SUB_MONTHLY_PRICE")
                .orElseThrow(() -> new CustomException(ErrorCode.SUBSCRIPTION_NOT_FOUND));

        SystemSetting durationSetting = systemSettingRepository.findBySettingKey("SUB_DURATION_DAYS")
                .orElseThrow(() -> new CustomException(ErrorCode.SYSTEM_SETTING_NOT_FOUND));

        long configPrice = Long.parseLong(priceSettiong.getSettingValue());
        int durationDays = Integer.parseInt(durationSetting.getSettingValue());

        if (requestDto.getAmount() != configPrice) {
            throw new CustomException(ErrorCode.INVALID_PAYMENT_AMOUNT);
        }

        LocalDateTime endDate = requestDto.getStartDate().plusDays(durationDays);

        boolean alreadyHas = subscriptionRepository.hasVehicleOverlappingSubscription(
                vehicle.getId(), requestDto.getStartDate(), endDate
        );
        if (alreadyHas) {
            throw new CustomException(ErrorCode.ALREADY_HAS_SUBSCRIPTION);
        }

        long currentSold = subscriptionRepository.countOverlappingActiveSubscriptions(
                requestDto.getStartDate(), endDate
        );

        if (currentSold >= Long.parseLong(maxCountSettiong.getSettingValue())) {
            throw new CustomException(ErrorCode.SUBSCRIPTION_SOLD_OUT);
        }

        String orderId = "SUB_" + UUID.randomUUID().toString().replace("-", "").substring(0, 10);

        Payment readyPayment = Payment.builder()
                .externalPaymentId(orderId)
                .amount(requestDto.getAmount())
                .priceSnapshot(requestDto.getAmount())
                .paymentMethod(PaymentMethod.PAY)
                .paymentType(PaymentType.SUBSCRIPTION)
                .paymentStatus(PaymentStatus.READY)
                .build();
        paymentRepository.save(readyPayment);

        log.info("[정기권 준비 완료] 유저 : {}, 차량: {}, 주문번호: {}",user.getName(), vehicle.getCarNumber(), orderId);

        return SubscriptionReadyResponseDto.builder()
                .orderId(orderId)
                .orderName("정기권 " + durationDays + "일 (" + requestDto.getCarNumber() + ")")
                .amount(requestDto.getAmount())
                .carNumber(requestDto.getCarNumber())
                .startDate(requestDto.getStartDate())
                .endDate(endDate)
                .build();
    }

    @Transactional
    public void confirmSubscription(Long userId, SubscriptionConfirmRequestDto confirmRequestDto) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        PaymentConfirmRequestDto internalDto = PaymentConfirmRequestDto.builder()
                .paymentKey(confirmRequestDto.getPaymentKey())
                .orderId(confirmRequestDto.getOrderId())
                .amount(confirmRequestDto.getAmount())
                .build();

        TossApprovalResult approvalResult = tossPaymentService.confirmAndAnalyze(internalDto);

        Payment payment = paymentRepository.findByExternalPaymentId(confirmRequestDto.getOrderId())
                .stream().findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_NOT_FOUND));

        // 토스 승인 실패 시 Payment를 FAILED로 변경 — READY 상태로 고아 레코드 남는 것 방지
        if (!approvalResult.isSuccess()) {
            payment.setPaymentStatus(PaymentStatus.FAILED);
            log.error("토스 승인 실패: {}", approvalResult.getErrorMessage());
            throw new CustomException(ErrorCode.PG_PROVIDER_ERROR);
        }

        // prepare 시 저장된 금액과 confirm 요청 금액이 다르면 차단 — 클라이언트 금액 변조 방지
        if (!payment.getAmount().equals(confirmRequestDto.getAmount())) {
            throw new CustomException(ErrorCode.INVALID_PAYMENT_AMOUNT);
        }

        Vehicle vehicle = vehicleRepository.findByCarNumber(confirmRequestDto.getCarNumber())
                .orElseThrow(() -> new CustomException(ErrorCode.VEHICLE_NOT_FOUND));

        if (vehicle.getUser() == null || !vehicle.getUser().getUserId().equals(userId)) {
            throw new CustomException(ErrorCode.ACCESS_DENIED_VEHICLE);
        }

        Subscription subscription = Subscription.builder()
                .user(user)
                .vehicle(vehicle)
                .startDate(confirmRequestDto.getStartDate())
                .endDate(confirmRequestDto.getEndDate())
                .status(Status.ACTIVE)
                .payment(payment)
                .price(confirmRequestDto.getAmount().intValue())
                .build();

        subscriptionRepository.save(subscription);

        payment.completePayment(approvalResult.getPaymentKey());

        // 정기권 구매 액티비티 로그 저장 — user, payment, carNumber 포함
        activityLogRepository.save(ActivityLog.builder()
                .activityType(ActivityType.PASS_PURCHASED)
                .user(user)                                          // 구매자 유저
                .payment(payment)                                    // 연결된 결제 내역
                .carNumber(vehicle.getCarNumber())                   // 정기권 등록 차량번호
                .message("정기권 구매 완료 (" + confirmRequestDto.getStartDate().toLocalDate()
                        + " ~ " + confirmRequestDto.getEndDate().toLocalDate() + ")")
                .build());

        log.info("[정기권 발급 완료] 유저: {}, 차량: {}, 결제성공 키: {}",
                user.getName(), vehicle.getCarNumber(), approvalResult.getPaymentKey());
    }

    @Transactional(readOnly = true)
    public SubscriptionPolicyResponseDto getSubscriptionPolicy(LocalDateTime targetDate) {
        long price = Long.parseLong(systemSettingRepository.findBySettingKey("SUB_MONTHLY_PRICE")
                .orElseThrow(() -> new CustomException(ErrorCode.SYSTEM_SETTING_NOT_FOUND)).getSettingValue());

        int durationDays = Integer.parseInt(systemSettingRepository.findBySettingKey("SUB_DURATION_DAYS")
                .orElseThrow(() -> new CustomException(ErrorCode.SYSTEM_SETTING_NOT_FOUND)).getSettingValue());

        long maxCount = Long.parseLong(systemSettingRepository.findBySettingKey("SUB_MAX_COUNT")
                .orElseThrow(() -> new CustomException(ErrorCode.SYSTEM_SETTING_NOT_FOUND)).getSettingValue());

        // 선택한 시작일 기준으로 현재 판매된 수량 조회 → 남은 슬롯 계산
        long soldCount = subscriptionRepository.countOverlappingActiveSubscriptions(
                targetDate, targetDate.plusDays(durationDays));
        long remainCount = Math.max(maxCount - soldCount, 0);

        return SubscriptionPolicyResponseDto.builder()
                .price(price)
                .durationDays(durationDays)
                .maxCount(maxCount)
                .remainCount(remainCount)
                .build();
    }

    @Transactional(readOnly = true)
    public List<SubscriptionHistoryResponseDto> getMySubscriptionHistory(Long userId) {
        return subscriptionRepository.findAllByUserId(userId).stream()
                .map(SubscriptionHistoryResponseDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public SubscriptionMyInfoResponseDto getMySubscription(Long userId) {
        // 현재 ACTIVE 상태인 정기권 조회 — 없으면 null 반환 (프론트에서 "정기권 없음" 처리)
        return subscriptionRepository.findMyActiveSubscription(userId, LocalDateTime.now())
                .map(SubscriptionMyInfoResponseDto::from)
                .orElse(null);
    }

    @Transactional
    public SubscriptionRefundResponseDto refundSubscription(Long userId, Long subscriptionId) {

        // vehicle, payment를 JOIN FETCH로 한 번에 조회 — 없으면 에러
        Subscription subscription = subscriptionRepository.findByIdWithVehicleAndPayment(subscriptionId)
                .orElseThrow(() -> new CustomException(ErrorCode.SUBSCRIPTION_NOT_FOUND));

        // 본인 정기권인지 확인 — 차량의 소유자 userId와 요청자 userId 비교
        if (!subscription.getVehicle().getUser().getUserId().equals(userId)) {
            throw new CustomException(ErrorCode.ACCESS_DENIED_VEHICLE);
        }

        // 이미 취소 또는 환불된 정기권은 재환불 불가
        if (subscription.getStatus() == Status.CANCELLED || subscription.getStatus() == Status.REFUNDED) {
            throw new CustomException(ErrorCode.SUBSCRIPTION_ALREADY_CANCELLED);
        }

        // 이미 만료된 정기권은 환불 불가
        if (subscription.getStatus() == Status.EXPIRED) {
            throw new CustomException(ErrorCode.SUBSCRIPTION_ALREADY_EXPIRED);
        }

        // 결제 내역 가져오기 — 토스 환불 호출에 paymentKey 필요
        Payment payment = subscription.getPayment();

        // 결제가 정상 완료된 건인지 확인 — SUCCESS가 아니면 토스에 환불 요청 불가
        if (payment.getPaymentStatus() != PaymentStatus.SUCCESS) {
            throw new CustomException(ErrorCode.PAYMENT_NOT_COMPLETED);
        }

        // 토스 결제 승인 시 저장된 paymentKey — 환불 API 호출에 사용
        String paymentKey = payment.getExternalPaymentId();

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startDate = subscription.getStartDate();
        LocalDateTime endDate = subscription.getEndDate();

        // 전체 이용 기간 (일수) — 예: 30일
        long totalDays = ChronoUnit.DAYS.between(startDate, endDate);

        long refundAmount;
        String refundType;

        if (now.isBefore(startDate)) {
            // 시작일 전에 취소 → 하루도 안 썼으므로 전액 환불
            refundAmount = payment.getAmount();
            refundType = "전액환불";
            tossPaymentService.cancelPayment(paymentKey, "정기권 전액 환불"); // 토스 전액 취소 API 호출
        } else {
            // 시작일 이후 취소 → 남은 일수만큼 일할 계산해서 부분 환불
            long usedDays = ChronoUnit.DAYS.between(startDate, now); // 오늘까지 사용한 일수
            long remainingDays = totalDays - usedDays;               // 남은 일수

            // 마지막 날(remainingDays=0)이면 환불할 금액이 없으므로 토스 호출 없이 그냥 취소만 처리
            if (remainingDays <= 0) {
                subscription.refund();
                payment.setPaymentStatus(PaymentStatus.REFUNDED);
                payment.setRefundedAmount(0L);
                log.info("[정기권 환불 완료] 유저: {}, 환불유형: 부분환불(0원), 마지막날 취소", userId);
                return SubscriptionRefundResponseDto.builder()
                        .refundAmount(0)
                        .refundType("부분환불")
                        .message("정기권이 취소되었습니다. 마지막 이용일이므로 환불 금액이 없습니다.")
                        .build();
            }

            // 곱셈 먼저 → 나눗셈 순서로 계산해야 소수점 손실 최소화 (원 단위 내림 처리)
            // 예: 100,000원 × 20일 / 30일 = 66,666원 (66,666.6...에서 내림)
            refundAmount = payment.getAmount() * remainingDays / totalDays;
            refundType = "부분환불";
            tossPaymentService.refundPayment(paymentKey, "정기권 부분 환불", (int) refundAmount); // 토스 부분 취소 API 호출
        }

        // 정기권 상태를 REFUNDED로 변경 — Subscription 엔티티의 refund() 메서드 호출
        subscription.refund();

        // 결제 상태 REFUNDED로 변경, 환불 금액 기록
        payment.setPaymentStatus(PaymentStatus.REFUNDED);
        payment.setRefundedAmount(refundAmount);

        // 환불 액티비티 로그 저장 — vehicle에서 user 꺼내서 포함
        User refundUser = subscription.getVehicle().getUser();
        activityLogRepository.save(ActivityLog.builder()
                .activityType(ActivityType.REFUNDED)
                .user(refundUser)                                    // 환불 요청 유저
                .payment(payment)                                    // 연결된 결제 내역
                .carNumber(subscription.getVehicle().getCarNumber()) // 정기권 등록 차량번호
                .message("정기권 " + refundType + " (" + refundAmount + "원)")
                .build());

        log.info("[정기권 환불 완료] 유저: {}, 환불유형: {}, 환불금액: {}원", userId, refundType, refundAmount);

        return SubscriptionRefundResponseDto.builder()
                .refundAmount(refundAmount)
                .refundType(refundType)
                .message("정기권이 취소되었습니다. " + refundAmount + "원이 환불됩니다.")
                .build();
    }

}
