package com.example.demo.domain.user.reservaion.service;

import com.example.demo.domain.shared.activityLog.ActivityLog;
import com.example.demo.domain.shared.activityLog.enums.ActivityType;
import com.example.demo.domain.shared.activityLog.repository.ActivityLogRepository;
import com.example.demo.domain.shared.approval.Approval;
import com.example.demo.domain.shared.approval.enums.ApprovalStatus;
import com.example.demo.domain.shared.approval.enums.ApprovalType;
import com.example.demo.domain.shared.approval.repository.ApprovalRepository;
import com.example.demo.domain.shared.household.Household;
import com.example.demo.domain.shared.household.enums.IsActive;
import com.example.demo.domain.shared.household.repository.HouseholdRepository;
import com.example.demo.domain.shared.parkinglog.repository.ParkingLogRepository;
import com.example.demo.domain.shared.reservation.Reservation;
import com.example.demo.domain.shared.reservation.repository.ReservationRepository;
import com.example.demo.domain.shared.reservationEventPolicy.ReservationEventPolicy;
import com.example.demo.domain.shared.reservationEventPolicy.repository.ReservationEventPolicyRepository;
import com.example.demo.domain.shared.subscription.repository.SubscriptionRepository;
import com.example.demo.domain.shared.systemSetting.SettingKey;
import com.example.demo.domain.shared.systemSetting.repository.SystemSettingRepository;
import com.example.demo.domain.shared.user.User;
import com.example.demo.domain.shared.user.UserRepository;
import com.example.demo.domain.shared.user.enums.Status;
import com.example.demo.domain.shared.vehicleblacklist.repository.VehicleBlacklistRepository;
import com.example.demo.domain.user.auth.principal.PrincipalDetails;
import com.example.demo.domain.user.reservaion.dtos.request.ReservationApplyRequestDto;
import com.example.demo.domain.user.reservaion.dtos.response.ReservationCancelResponseDto;
import com.example.demo.domain.user.reservaion.dtos.response.ReservationDetailResponseDto;
import com.example.demo.domain.user.reservaion.dtos.response.ReservationEventPolicyResponseDto;
import com.example.demo.domain.user.reservaion.dtos.response.ReservationListResponseDto;
import com.example.demo.global.exception.CustomException;
import com.example.demo.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ApprovalRepository approvalRepository;
    private final VehicleBlacklistRepository vehicleBlacklistRepository;
    private final ReservationEventPolicyRepository reservationEventPolicyRepository;
    private final ParkingLogRepository parkingLogRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final HouseholdRepository householdRepository;
    private final ActivityLogRepository activityLogRepository;
    private final UserRepository userRepository;
    private final SystemSettingRepository systemSettingRepository;

    // --- [시간 계산 공통 헬퍼 메서드] ---
    private LocalDateTime getStartOfDay() { return LocalDate.now().atStartOfDay(); }
    private LocalDateTime getEndOfDay() { return LocalDate.now().atTime(LocalTime.MAX); }
    private LocalDateTime getStartOfMonth() { return LocalDate.now().withDayOfMonth(1).atStartOfDay(); }

    private LocalDateTime getStartOfDate(LocalDate date) { return date.atStartOfDay(); }
    private LocalDateTime getEndOfDate(LocalDate date) { return date.atTime(LocalTime.MAX); }
    /**
     * [방문 예약 신청]
     */
    @Transactional
    public ReservationDetailResponseDto applyReservation(PrincipalDetails principalDetails, ReservationApplyRequestDto reservationApplyRequestDto) {

        // [검증 1] 신청 유저 및 해당 세대 활성화 상태 확인
        User user = getValidatedUserAndHousehold(principalDetails.getUser().getUserId());
        Household household = user.getHousehold();

        LocalDate visitDate = reservationApplyRequestDto.getVisitStartAt().toLocalDate();

        // [검증 2] 당일 예약 신청 방지 및 방문 날짜 추출
        LocalDate today = LocalDate.now();
        if (!visitDate.isAfter(today)) {
            throw new CustomException(ErrorCode.RESERVATION_NOT_TODAY);
        }

        // [검증 3] 시스템 전체 일일 예약 제한 확인 (선택한 방문 날짜 기준!!)
        validateSystemTotalLimit(visitDate);

        String carNumber = reservationApplyRequestDto.getCarNumber();
        LocalDateTime now = LocalDateTime.now();

        // [검증 4] 블랙리스트 차량 여부 확인
        if(vehicleBlacklistRepository.isCurrentlyBlacklisted(carNumber, now)) {
            throw new CustomException(ErrorCode.BLACKLIST_VEHICLE);
        }

        // [검증 5] 해당 차량의 활성화된 정기권 존재 여부 확인
        if(subscriptionRepository.hasActiveSubscription(carNumber, now)) {
            throw new CustomException(ErrorCode.ACTIVE_SUBSCRIPTION_EXISTS);
        }

        // [검증 6] 해당 차량이 현재 단지 내에 이미 입차해 있는지 확인
        if(parkingLogRepository.isAlreadyInParkingLot(carNumber)) {
            throw new CustomException(ErrorCode.VEHICLE_ALREADY_ENTERED);
        }

        // [검증 7] 해당 차량 번호로 이미 신청된 '대기' 또는 '승인' 상태의 예약 존재 여부 확인
        var PENDING = com.example.demo.domain.shared.reservation.enums.Status.PENDING;
        var RESERVED = com.example.demo.domain.shared.reservation.enums.Status.RESERVED;
        if(reservationRepository.existsByCarNumberAndStatusIn(carNumber, List.of(PENDING, RESERVED))) {
            throw new CustomException(ErrorCode.ALREADY_RESERVED_VEHICLE);
        }

        // [검증 8] 현재 주차 관리 정책(Policy) 조회 및 위반 확인
        ReservationEventPolicy policy = reservationEventPolicyRepository.findActivePolicy(now)
                .orElseThrow(() -> new CustomException(ErrorCode.PARKING_POLICY_NOT_FOUND));

        // (8-1) 세대별 동시 보유 가능한 활성 예약증 수 제한 확인
        if(household.getActiveReservationCount() >= policy.getMaxActiveReservations()) {
            throw new CustomException(ErrorCode.MAX_RESERVATION_EXCEEDED);
        }

        // (8-2) 세대별 방문 예정일의 예약 신청 횟수 제한 확인 (방문일 기준!!)
        long visitDateCount = reservationRepository.countDailyReservations(household.getHouseholdId(), getStartOfDate(visitDate), getEndOfDate(visitDate));
        if(policy.getDailyLimitPerHousehold() != null && visitDateCount >= policy.getDailyLimitPerHousehold()) {
            throw new CustomException(ErrorCode.DAILY_LIMIT_EXCEEDED);
        }

        // 예약 생성 및 저장
        LocalDateTime visitStartAt = reservationApplyRequestDto.getVisitStartAt();
        int minutesToAdd = (policy.getPermittedMinutes() != null) ? policy.getPermittedMinutes() : 60;
        LocalDateTime visitEndAt = visitStartAt.plusMinutes(minutesToAdd);

        Reservation reservation = Reservation.builder()
                .user(user).carNumber(carNumber).purpose(reservationApplyRequestDto.getPurpose())
                .visitStartAt(visitStartAt).visitEndAt(visitEndAt).status(PENDING).isFree(true).build();

        Reservation savedReservation = reservationRepository.save(reservation);

        // 관리자 결재 요청 및 활동 로그 생성
        approvalRepository.save(Approval.builder()
                .approvalType(ApprovalType.RESERVATION).targetId(savedReservation.getReservationId())
                .requestUserId(user).status(ApprovalStatus.PENDING).build());

        activityLogRepository.save(ActivityLog.builder()
                .activityType(ActivityType.RESERVATION_CREATED).reservation(savedReservation).carNumber(carNumber)
                .household(household).message(String.format("[%s] 차량 방문 예약 신청", carNumber)).build());

        // 세대 활성 예약 카운트 증가
        householdRepository.incrementActiveReservationCount(household.getHouseholdId());

        return ReservationDetailResponseDto.fromEntity(savedReservation);
    }

    /**
     * [방문 예약 정책 및 특정 날짜의 잔여 현황 조회]
     * @param targetDate 사용자가 달력에서 클릭한 미래의 날짜
     */
    @Transactional(readOnly = true)
    public ReservationEventPolicyResponseDto getReservationPolicyInfo(PrincipalDetails principalDetails, LocalDate targetDate) {

        User user = getValidatedUserAndHousehold(principalDetails.getUser().getUserId());
        Household household = user.getHousehold();

        // 1. 시스템 공통 설정 조회 (전체 제한)
        int totalDailyLimit = systemSettingRepository.findBySettingKey(SettingKey.TOTAL_DAILY_RESERVATION_LIMIT.getKey())
                .map(s -> Integer.parseInt(s.getSettingValue()))
                .orElse(SettingKey.TOTAL_DAILY_RESERVATION_LIMIT.getDefaultIntValue());

        // 2. 선택한 날짜(Target Date)의 실시간 현황 집계
        long targetDateTotalCount = reservationRepository.countAllDailyReservations(getStartOfDate(targetDate), getEndOfDate(targetDate));
        long targetDateUserCount = reservationRepository.countDailyReservations(household.getHouseholdId(), getStartOfDate(targetDate), getEndOfDate(targetDate));

        // 3. 정책 정보 및 사용자 누적 상태 조회
        ReservationEventPolicy policy = reservationEventPolicyRepository.findActivePolicy(LocalDateTime.now())
                .orElseThrow(() -> new CustomException(ErrorCode.PARKING_POLICY_NOT_FOUND));

        long monthUsedCount = reservationRepository.countMonthlyReservations(household.getHouseholdId(), getStartOfMonth());

        var ACTIVE_STATUSES = List.of(com.example.demo.domain.shared.reservation.enums.Status.PENDING,
                com.example.demo.domain.shared.reservation.enums.Status.RESERVED);
        int currentActiveCount = (int) reservationRepository.countByHouseholdIdAndStatusIn(household.getHouseholdId(), ACTIVE_STATUSES);

        // 4. 메시지 동적 생성
        String systemMessage = (targetDateTotalCount >= totalDailyLimit)
                ? targetDate + "일은 이미 모든 예약이 마감되었습니다."
                : null;

        String warningMessage = "본 예약은 주차 공간을 확정적으로 보장하지 않으며, 현장 만차 시 입차가 제한될 수 있습니다.";
        if (targetDateTotalCount >= (totalDailyLimit * 0.8) && systemMessage == null) {
            warningMessage = "현재 해당 날짜의 예약량이 많아 주차가 혼잡할 수 있습니다. " + warningMessage;
        }

        return new ReservationEventPolicyResponseDto(
                policy.getEventName(),
                policy.getPermittedMinutes() != null ? policy.getPermittedMinutes() : 60,
                policy.getDailyLimitPerHousehold() != null ? policy.getDailyLimitPerHousehold() : 0,
                policy.getMonthlyLimitPerHousehold() != null ? policy.getMonthlyLimitPerHousehold() : 0,
                policy.getMaxActiveReservations(),
                totalDailyLimit,
                monthUsedCount,
                currentActiveCount,
                targetDateTotalCount,
                targetDateUserCount,
                policy.isNoShowPenaltyEnabled(),
                systemMessage,
                warningMessage
        );
    }

    /**
     * [내 방문 예약 내역 조회]
     */
    @Transactional(readOnly = true)
    public List<ReservationListResponseDto> getMyReservations(PrincipalDetails principalDetails) {

        // [검증] 유저 및 세대 활성 상태 확인 (유저 객체만 필요)
        User user = getValidatedUserAndHousehold(principalDetails.getUser().getUserId());

        return reservationRepository.findByUserOrderByCreatedAtDesc(user)
                .stream().map(ReservationListResponseDto::new).toList();
    }

    /**
     * [방문 예약 취소]
     */
    @Transactional
    public ReservationCancelResponseDto cancelReservation(PrincipalDetails principalDetails, Long reservationId) {
        var PENDING = com.example.demo.domain.shared.reservation.enums.Status.PENDING;
        var RESERVED = com.example.demo.domain.shared.reservation.enums.Status.RESERVED;
        var CANCELLED = com.example.demo.domain.shared.reservation.enums.Status.CANCELLED;

        // [검증 1] 유저 및 세대 활성 상태 확인
        User user = getValidatedUserAndHousehold(principalDetails.getUser().getUserId());

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESERVATION_NOT_FOUND));

        // [검증 2] 당일/과거 예약 취소 금지 (예약 시작일이 내일 이후여야 함)
        LocalDate today = LocalDate.now();
        if (!reservation.getVisitStartAt().toLocalDate().isAfter(today)) {
            throw new CustomException(ErrorCode.CANCEL_NOT_TODAY);
        }

        // [검증 3] 본인의 예약인지 확인
        if (!reservation.getUser().getUserId().equals(user.getUserId())) {
            throw new CustomException(ErrorCode.RESERVATION_NOT_OWNER);
        }

        // [검증 4] 이미 취소된 상태인지 확인
        if (reservation.getStatus() == CANCELLED) {
            throw new CustomException(ErrorCode.RESERVATION_ALREADY_CANCELLED);
        }

        // [검증 5] 취소 가능한 상태(PENDING, RESERVED)인지 확인
        if (reservation.getStatus() != PENDING && reservation.getStatus() != RESERVED) {
            throw new CustomException(ErrorCode.RESERVATION_CANNOT_CANCEL_STATUS);
        }

        // [검증 6] 예약 차량이 이미 단지에 들어와 있는지 확인
        if (parkingLogRepository.isAlreadyInParkingLot(reservation.getCarNumber())) {
            throw new CustomException(ErrorCode.RESERVATION_ALREADY_USED);
        }

        // 취소 처리 및 세대 활성 예약 카운트 차감
        reservation.cancel(CANCELLED);
        householdRepository.decrementActiveReservationCount(user.getHousehold().getHouseholdId());

        // 결재 요청이 대기 중일 경우 자동 취소 처리
        approvalRepository.findByTargetIdAndApprovalType(reservationId, ApprovalType.RESERVATION)
                .ifPresent(approval -> { if (approval.getStatus() == ApprovalStatus.PENDING) approval.updateStatus(ApprovalStatus.CANCELLED); });

        return new ReservationCancelResponseDto(reservation);
    }

    /**
     * [공통 헬퍼] 아파트 전체 일일 예약 제한 검증
     */
    private void validateSystemTotalLimit(LocalDate targetDate) {
        int totalLimit = systemSettingRepository.findBySettingKey(SettingKey.TOTAL_DAILY_RESERVATION_LIMIT.getKey())
                .map(s -> Integer.parseInt(s.getSettingValue()))
                .orElse(SettingKey.TOTAL_DAILY_RESERVATION_LIMIT.getDefaultIntValue());

        long count = reservationRepository.countAllDailyReservations(getStartOfDate(targetDate), getEndOfDate(targetDate));

        if (count >= totalLimit) {
            throw new CustomException(ErrorCode.SYSTEM_TOTAL_DAILY_LIMIT_EXCEEDED);
        }
    }
    /**
     * [공통 헬퍼] 유저와 세대의 활성 상태를 모두 검증합니다.
     */
    private User getValidatedUserAndHousehold(Long userId) {
        // 유저 존재 및 상태 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (user.getStatus() != Status.ACTIVE) {
            throw new CustomException(ErrorCode.USER_SUSPENDED);
        }

        // 세대 존재 및 상태 확인 (비거주자 또는 비활성 세대 차단)
        Household household = user.getHousehold();
        if (household == null || household.getIsActive() != IsActive.ACTIVE) {
            throw new CustomException(ErrorCode.NOT_AVAILABLE_HOUSEHOLD);
        }

        return user;
    }
}