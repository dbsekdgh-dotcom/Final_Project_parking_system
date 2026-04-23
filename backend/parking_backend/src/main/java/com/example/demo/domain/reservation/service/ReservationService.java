package com.example.demo.domain.reservation.service;

import com.example.demo.domain.system.activitylog.ActivityLog;
import com.example.demo.domain.system.activitylog.enums.ActivityType;
import com.example.demo.domain.system.activitylog.repository.ActivityLogRepository;
import com.example.demo.domain.approval.Approval;
import com.example.demo.domain.approval.enums.ApprovalStatus;
import com.example.demo.domain.approval.enums.ApprovalType;
import com.example.demo.domain.approval.repository.ApprovalRepository;
import com.example.demo.domain.resident.household.Household;
import com.example.demo.domain.resident.household.enums.IsActive;
import com.example.demo.domain.resident.household.repository.HouseholdRepository;
import com.example.demo.domain.parking.log.repository.ParkingLogRepository;
import com.example.demo.domain.reservation.Reservation;
import com.example.demo.domain.reservation.repository.ReservationRepository;
import com.example.demo.domain.reservation.policy.ReservationEventPolicy;
import com.example.demo.domain.reservation.policy.repository.ReservationEventPolicyRepository;
import com.example.demo.domain.payment.subscription.repository.SubscriptionRepository;
import com.example.demo.domain.system.setting.SettingKey;
import com.example.demo.domain.system.setting.repository.SystemSettingRepository;
import com.example.demo.domain.resident.User;
import com.example.demo.domain.resident.UserRepository;
import com.example.demo.domain.resident.enums.Status;
import com.example.demo.domain.vehicle.blacklist.repository.VehicleBlacklistRepository;
import com.example.demo.domain.auth.user.principal.PrincipalDetails;
import com.example.demo.domain.reservation.dtos.request.ReservationApplyRequestDto;
import com.example.demo.domain.reservation.dtos.response.ReservationCancelResponseDto;
import com.example.demo.domain.reservation.dtos.response.ReservationDetailResponseDto;
import com.example.demo.domain.reservation.dtos.response.ReservationEventPolicyResponseDto;
import com.example.demo.domain.reservation.dtos.response.ReservationListResponseDto;
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

    private LocalDateTime getStartOfDay() { return LocalDate.now().atStartOfDay(); }
    private LocalDateTime getEndOfDay() { return LocalDate.now().atTime(LocalTime.MAX); }
    private LocalDateTime getStartOfMonth() { return LocalDate.now().withDayOfMonth(1).atStartOfDay(); }
    private LocalDateTime getStartOfDate(LocalDate date) { return date.atStartOfDay(); }
    private LocalDateTime getEndOfDate(LocalDate date) { return date.atTime(LocalTime.MAX); }

    @Transactional
    public ReservationDetailResponseDto applyReservation(PrincipalDetails principalDetails, ReservationApplyRequestDto reservationApplyRequestDto) {

        User user = getValidatedUserAndHousehold(principalDetails.getUser().getUserId());
        Household household = user.getHousehold();
        LocalDate visitDate = reservationApplyRequestDto.getVisitStartAt().toLocalDate();

        if (!visitDate.isAfter(LocalDate.now())) {
            throw new CustomException(ErrorCode.RESERVATION_NOT_TODAY);
        }

        validateSystemTotalLimit(visitDate);

        String carNumber = reservationApplyRequestDto.getCarNumber();
        LocalDateTime now = LocalDateTime.now();

        if (vehicleBlacklistRepository.isCurrentlyBlacklisted(carNumber, now)) {
            throw new CustomException(ErrorCode.BLACKLIST_VEHICLE);
        }
        if (subscriptionRepository.hasActiveSubscription(carNumber, now)) {
            throw new CustomException(ErrorCode.ACTIVE_SUBSCRIPTION_EXISTS);
        }
        if (parkingLogRepository.isAlreadyInParkingLot(carNumber)) {
            throw new CustomException(ErrorCode.VEHICLE_ALREADY_ENTERED);
        }

        var PENDING = com.example.demo.domain.reservation.enums.Status.PENDING;
        var RESERVED = com.example.demo.domain.reservation.enums.Status.RESERVED;
        if (reservationRepository.existsByCarNumberAndStatusIn(carNumber, List.of(PENDING, RESERVED))) {
            throw new CustomException(ErrorCode.ALREADY_RESERVED_VEHICLE);
        }

        ReservationEventPolicy policy = reservationEventPolicyRepository.findActivePolicy(now)
                .orElseThrow(() -> new CustomException(ErrorCode.PARKING_POLICY_NOT_FOUND));

        if (household.getActiveReservationCount() >= policy.getMaxActiveReservations()) {
            throw new CustomException(ErrorCode.MAX_RESERVATION_EXCEEDED);
        }

        long visitDateCount = reservationRepository.countDailyReservations(
                household.getHouseholdId(), getStartOfDate(visitDate), getEndOfDate(visitDate));
        if (policy.getDailyLimitPerHousehold() != null && visitDateCount >= policy.getDailyLimitPerHousehold()) {
            throw new CustomException(ErrorCode.DAILY_LIMIT_EXCEEDED);
        }

        long monthCount = reservationRepository.countMonthlyReservations(
                household.getHouseholdId(), getStartOfMonth());
        if (policy.getMonthlyLimitPerHousehold() != null && monthCount >= policy.getMonthlyLimitPerHousehold()) {
            throw new CustomException(ErrorCode.MONTHLY_LIMIT_EXCEEDED);
        }

        LocalDateTime visitStartAt = reservationApplyRequestDto.getVisitStartAt();
        int minutesToAdd = (policy.getPermittedMinutes() != null) ? policy.getPermittedMinutes() : 60;
        LocalDateTime visitEndAt = visitStartAt.plusMinutes(minutesToAdd);

        Reservation reservation = Reservation.builder()
                .user(user).carNumber(carNumber).purpose(reservationApplyRequestDto.getPurpose())
                .visitStartAt(visitStartAt).visitEndAt(visitEndAt).status(PENDING).isFree(true).build();

        Reservation savedReservation = reservationRepository.save(reservation);

        approvalRepository.save(Approval.builder()
                .approvalType(ApprovalType.RESERVATION).targetId(savedReservation.getReservationId())
                .requestUserId(user).status(ApprovalStatus.PENDING).build());

        activityLogRepository.save(ActivityLog.builder()
                .activityType(ActivityType.RESERVATION_CREATED).reservation(savedReservation).carNumber(carNumber)
                .household(household).message(String.format("[%s] 차량 방문 예약 신청", carNumber)).build());

        householdRepository.incrementActiveReservationCount(household.getHouseholdId());
        householdRepository.incrementTotalVisitCount(household.getHouseholdId());

        return ReservationDetailResponseDto.fromEntity(savedReservation);
    }

    @Transactional(readOnly = true)
    public ReservationEventPolicyResponseDto getReservationPolicyInfo(PrincipalDetails principalDetails, LocalDate targetDate) {

        User user = getValidatedUserAndHousehold(principalDetails.getUser().getUserId());
        Household household = user.getHousehold();

        int totalDailyLimit = systemSettingRepository.findBySettingKey(SettingKey.TOTAL_DAILY_RESERVATION_LIMIT.getKey())
                .map(s -> Integer.parseInt(s.getSettingValue()))
                .orElse(SettingKey.TOTAL_DAILY_RESERVATION_LIMIT.getDefaultIntValue());

        long targetDateTotalCount = reservationRepository.countAllDailyReservations(
                getStartOfDate(targetDate), getEndOfDate(targetDate));
        long targetDateUserCount = reservationRepository.countDailyReservations(
                household.getHouseholdId(), getStartOfDate(targetDate), getEndOfDate(targetDate));

        ReservationEventPolicy policy = reservationEventPolicyRepository.findActivePolicy(LocalDateTime.now())
                .orElseThrow(() -> new CustomException(ErrorCode.PARKING_POLICY_NOT_FOUND));

        long monthUsedCount = reservationRepository.countMonthlyReservations(
                household.getHouseholdId(), getStartOfMonth());

        var ACTIVE_STATUSES = List.of(
                com.example.demo.domain.reservation.enums.Status.PENDING,
                com.example.demo.domain.reservation.enums.Status.RESERVED);
        int currentActiveCount = (int) reservationRepository.countByHouseholdIdAndStatusIn(
                household.getHouseholdId(), ACTIVE_STATUSES);

        String systemMessage = (targetDateTotalCount >= totalDailyLimit)
                ? targetDate + "일은 이미 모든 예약이 마감되었습니다." : null;
        String warningMessage = "본 예약은 주차 공간을 확정적으로 보장하지 않으며, 현장 만차 시 입차가 제한될 수 있습니다.";
        if (targetDateTotalCount >= (totalDailyLimit * 0.8) && systemMessage == null) {
            warningMessage = "현재 해당 날짜의 예약량이 많아 주차가 혼잡할 수 있습니다. " + warningMessage;
        }

        return new ReservationEventPolicyResponseDto(
                policy.getEventName(),
                policy.getPermittedMinutes() != null ? policy.getPermittedMinutes() : 60,
                policy.getDailyLimitPerHousehold() != null ? policy.getDailyLimitPerHousehold() : 0,
                policy.getMonthlyLimitPerHousehold() != null ? policy.getMonthlyLimitPerHousehold() : 0,
                policy.getMaxActiveReservations(), totalDailyLimit, monthUsedCount, currentActiveCount,
                targetDateTotalCount, targetDateUserCount,
                policy.isNoShowPenaltyEnabled(), systemMessage, warningMessage);
    }

    @Transactional(readOnly = true)
    public List<ReservationListResponseDto> getMyReservations(PrincipalDetails principalDetails) {
        User user = getValidatedUserAndHousehold(principalDetails.getUser().getUserId());
        return reservationRepository.findByUserOrderByCreatedAtDesc(user)
                .stream().map(ReservationListResponseDto::new).toList();
    }

    @Transactional
    public ReservationCancelResponseDto cancelReservation(PrincipalDetails principalDetails, Long reservationId) {
        var PENDING = com.example.demo.domain.reservation.enums.Status.PENDING;
        var RESERVED = com.example.demo.domain.reservation.enums.Status.RESERVED;
        var CANCELLED = com.example.demo.domain.reservation.enums.Status.CANCELLED;

        User user = getValidatedUserAndHousehold(principalDetails.getUser().getUserId());
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESERVATION_NOT_FOUND));

        if (!reservation.getVisitStartAt().toLocalDate().isAfter(LocalDate.now())) {
            throw new CustomException(ErrorCode.CANCEL_NOT_TODAY);
        }
        if (!reservation.getUser().getUserId().equals(user.getUserId())) {
            throw new CustomException(ErrorCode.RESERVATION_NOT_OWNER);
        }
        if (reservation.getStatus() == CANCELLED) {
            throw new CustomException(ErrorCode.RESERVATION_ALREADY_CANCELLED);
        }
        if (reservation.getStatus() != PENDING && reservation.getStatus() != RESERVED) {
            throw new CustomException(ErrorCode.RESERVATION_CANNOT_CANCEL_STATUS);
        }
        if (parkingLogRepository.isAlreadyInParkingLot(reservation.getCarNumber())) {
            throw new CustomException(ErrorCode.RESERVATION_ALREADY_USED);
        }

        reservation.cancel(CANCELLED);
        householdRepository.decrementActiveReservationCount(user.getHousehold().getHouseholdId());

        approvalRepository.findByTargetIdAndApprovalType(reservationId, ApprovalType.RESERVATION)
                .ifPresent(approval -> {
                    if (approval.getStatus() == ApprovalStatus.PENDING)
                        approval.updateStatus(ApprovalStatus.CANCELLED);
                });

        return new ReservationCancelResponseDto(reservation);
    }

    @Transactional
    public ReservationDetailResponseDto updateReservation(PrincipalDetails principalDetails, Long reservationId, ReservationApplyRequestDto reservationApplyRequestDto) {

        User user = getValidatedUserAndHousehold(principalDetails.getUser().getUserId());
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESERVATION_NOT_FOUND));

        if (!reservation.getUser().getUserId().equals(user.getUserId())) {
            throw new CustomException(ErrorCode.RESERVATION_NOT_OWNER);
        }

        var PENDING = com.example.demo.domain.reservation.enums.Status.PENDING;
        var RESERVED = com.example.demo.domain.reservation.enums.Status.RESERVED;
        if (reservation.getStatus() != PENDING) {
            throw new CustomException(ErrorCode.RESERVATION_CANNOT_EDIT_STATUS);
        }

        String newCarNumber = reservationApplyRequestDto.getCarNumber();
        LocalDate newVisitDate = reservationApplyRequestDto.getVisitStartAt().toLocalDate();
        LocalDate oldVisitDate = reservation.getVisitStartAt().toLocalDate();
        LocalDateTime now = LocalDateTime.now();

        if (vehicleBlacklistRepository.isCurrentlyBlacklisted(newCarNumber, now)) {
            throw new CustomException(ErrorCode.BLACKLIST_VEHICLE);
        }
        if (subscriptionRepository.hasActiveSubscription(newCarNumber, now)) {
            throw new CustomException(ErrorCode.ACTIVE_SUBSCRIPTION_EXISTS);
        }
        if (parkingLogRepository.isAlreadyInParkingLot(newCarNumber)) {
            throw new CustomException(ErrorCode.VEHICLE_ALREADY_ENTERED);
        }
        if (reservationRepository.existsByCarNumberAndStatusInAndReservationIdNot(
                newCarNumber, List.of(PENDING, RESERVED), reservationId)) {
            throw new CustomException(ErrorCode.ALREADY_RESERVED_VEHICLE);
        }

        ReservationEventPolicy policy = reservationEventPolicyRepository.findActivePolicy(now)
                .orElseThrow(() -> new CustomException(ErrorCode.PARKING_POLICY_NOT_FOUND));

        if (!newVisitDate.equals(oldVisitDate)) {
            if (!newVisitDate.isAfter(LocalDate.now())) {
                throw new CustomException(ErrorCode.RESERVATION_NOT_TODAY);
            }
            validateSystemTotalLimit(newVisitDate);

            long visitDateCount = reservationRepository.countDailyReservations(
                    user.getHousehold().getHouseholdId(),
                    getStartOfDate(newVisitDate), getEndOfDate(newVisitDate));
            if (policy.getDailyLimitPerHousehold() != null && visitDateCount >= policy.getDailyLimitPerHousehold()) {
                throw new CustomException(ErrorCode.DAILY_LIMIT_EXCEEDED);
            }
        } else {
            // 날짜는 그대로지만 당일 이하면 수정 불가 (취소와 동일 기준)
            if (!oldVisitDate.isAfter(LocalDate.now())) {
                throw new CustomException(ErrorCode.CANCEL_NOT_TODAY);
            }
        }

        int permittedMinutes = (policy.getPermittedMinutes() != null) ? policy.getPermittedMinutes() : 60;
        reservation.setCarNumber(newCarNumber);
        reservation.setPurpose(reservationApplyRequestDto.getPurpose());
        reservation.setVisitStartAt(reservationApplyRequestDto.getVisitStartAt());
        reservation.setVisitEndAt(reservationApplyRequestDto.getVisitStartAt().plusMinutes(permittedMinutes));

        return ReservationDetailResponseDto.fromEntity(reservation);
    }

    private void validateSystemTotalLimit(LocalDate targetDate) {
        int totalLimit = systemSettingRepository.findBySettingKey(SettingKey.TOTAL_DAILY_RESERVATION_LIMIT.getKey())
                .map(s -> Integer.parseInt(s.getSettingValue()))
                .orElse(SettingKey.TOTAL_DAILY_RESERVATION_LIMIT.getDefaultIntValue());

        long count = reservationRepository.countAllDailyReservations(
                getStartOfDate(targetDate), getEndOfDate(targetDate));

        if (count >= totalLimit) {
            throw new CustomException(ErrorCode.SYSTEM_TOTAL_DAILY_LIMIT_EXCEEDED);
        }
    }

    private User getValidatedUserAndHousehold(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (user.getStatus() != Status.ACTIVE) {
            throw new CustomException(ErrorCode.USER_SUSPENDED);
        }

        Household household = user.getHousehold();
        if (household == null || household.getIsActive() != IsActive.ACTIVE) {
            throw new CustomException(ErrorCode.NOT_AVAILABLE_HOUSEHOLD);
        }

        return user;
    }
}
