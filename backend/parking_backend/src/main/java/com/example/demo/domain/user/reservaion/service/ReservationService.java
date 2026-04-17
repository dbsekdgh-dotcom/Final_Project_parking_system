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
    // 오늘 기준 시작 시간
    private LocalDateTime getStartOfDay() { return LocalDate.now().atStartOfDay(); }
    // 오늘 기준 종료 시간
    private LocalDateTime getEndOfDay() { return LocalDate.now().atTime(LocalTime.MAX); }
    // 이번 달 1일 시작 시간
    private LocalDateTime getStartOfMonth() { return LocalDate.now().withDayOfMonth(1).atStartOfDay(); }
    // 특정 날짜의 시작 시간 (00:00:00)
    private LocalDateTime getStartOfDate(LocalDate date) { return date.atStartOfDay(); }
    // 특정 날짜의 종료 시간 (23:59:59)
    private LocalDateTime getEndOfDate(LocalDate date) { return date.atTime(LocalTime.MAX); }

    /**
     * [방문 예약 신청]
     * 신규 방문 예약을 생성하고 관리자 승인 프로세스를 시작합니다.
     */
    @Transactional
    public ReservationDetailResponseDto applyReservation(PrincipalDetails principalDetails, ReservationApplyRequestDto reservationApplyRequestDto) {

        // 1. 신청 유저 및 해당 세대 활성화 상태 확인
        User user = getValidatedUserAndHousehold(principalDetails.getUser().getUserId());
        Household household = user.getHousehold();

        LocalDate visitDate = reservationApplyRequestDto.getVisitStartAt().toLocalDate();

        // 2. 당일 예약 신청 방지 (최소 하루 전 신청 원칙)
        LocalDate today = LocalDate.now();
        if (!visitDate.isAfter(today)) {
            throw new CustomException(ErrorCode.RESERVATION_NOT_TODAY);
        }

        // 3. 아파트 전체 일일 예약 제한 확인 (선택한 방문 예정일 기준)
        validateSystemTotalLimit(visitDate);

        String carNumber = reservationApplyRequestDto.getCarNumber();
        LocalDateTime now = LocalDateTime.now();

        // 4. 블랙리스트 차량 여부 확인
        if(vehicleBlacklistRepository.isCurrentlyBlacklisted(carNumber, now)) {
            throw new CustomException(ErrorCode.BLACKLIST_VEHICLE);
        }

        // 5. 정기권 차량 여부 확인 (정기권은 별도 예약 불필요)
        if(subscriptionRepository.hasActiveSubscription(carNumber, now)) {
            throw new CustomException(ErrorCode.ACTIVE_SUBSCRIPTION_EXISTS);
        }

        // 6. 현재 단지 내 입차 여부 확인 (중복 입차 방지)
        if(parkingLogRepository.isAlreadyInParkingLot(carNumber)) {
            throw new CustomException(ErrorCode.VEHICLE_ALREADY_ENTERED);
        }

        // 7. 동일 차량의 활성화된 예약(PENDING, RESERVED) 중복 존재 확인
        var PENDING = com.example.demo.domain.shared.reservation.enums.Status.PENDING;
        var RESERVED = com.example.demo.domain.shared.reservation.enums.Status.RESERVED;
        if(reservationRepository.existsByCarNumberAndStatusIn(carNumber, List.of(PENDING, RESERVED))) {
            throw new CustomException(ErrorCode.ALREADY_RESERVED_VEHICLE);
        }

        // 8. 주차 관리 정책 위반 확인
        ReservationEventPolicy policy = reservationEventPolicyRepository.findActivePolicy(now)
                .orElseThrow(() -> new CustomException(ErrorCode.PARKING_POLICY_NOT_FOUND));

        // (8-1) 세대당 동시 보유 가능한 최대 예약증 수 체크
        if(household.getActiveReservationCount() >= policy.getMaxActiveReservations()) {
            throw new CustomException(ErrorCode.MAX_RESERVATION_EXCEEDED);
        }

        // (8-2) 해당 세대의 방문 예정일 일일 신청 횟수 제한 체크
        long visitDateCount = reservationRepository.countDailyReservations(household.getHouseholdId(), getStartOfDate(visitDate), getEndOfDate(visitDate));
        if(policy.getDailyLimitPerHousehold() != null && visitDateCount >= policy.getDailyLimitPerHousehold()) {
            throw new CustomException(ErrorCode.DAILY_LIMIT_EXCEEDED);
        }

        // 9. 예약 엔티티 생성 및 저장
        LocalDateTime visitStartAt = reservationApplyRequestDto.getVisitStartAt();
        int minutesToAdd = (policy.getPermittedMinutes() != null) ? policy.getPermittedMinutes() : 60;
        LocalDateTime visitEndAt = visitStartAt.plusMinutes(minutesToAdd);

        Reservation reservation = Reservation.builder()
                .user(user).carNumber(carNumber).purpose(reservationApplyRequestDto.getPurpose())
                .visitStartAt(visitStartAt).visitEndAt(visitEndAt).status(PENDING).isFree(true).build();

        Reservation savedReservation = reservationRepository.save(reservation);

        // 10. 관리자 결재 요청 생성
        approvalRepository.save(Approval.builder()
                .approvalType(ApprovalType.RESERVATION).targetId(savedReservation.getReservationId())
                .requestUserId(user).status(ApprovalStatus.PENDING).build());

        // 11. 활동 로그 기록
        activityLogRepository.save(ActivityLog.builder()
                .activityType(ActivityType.RESERVATION_CREATED).reservation(savedReservation).carNumber(carNumber)
                .user(user).household(household).message(String.format("[%s] 차량 방문 예약 신청", carNumber)).build());

        // 12. 세대 활성 예약 카운트 증가
        householdRepository.incrementActiveReservationCount(household.getHouseholdId());

        return ReservationDetailResponseDto.fromEntity(savedReservation);
    }

    /**
     * [방문 예약 정책 및 잔여 현황 조회]
     * 특정 날짜의 시스템 전체 예약 현황과 개인 정책 준수 상태를 반환합니다.
     */
    @Transactional(readOnly = true)
    public ReservationEventPolicyResponseDto getReservationPolicyInfo(PrincipalDetails principalDetails, LocalDate targetDate) {

        User user = getValidatedUserAndHousehold(principalDetails.getUser().getUserId());
        Household household = user.getHousehold();

        // 1. 시스템 설정값(전체 일일 제한) 조회
        int totalDailyLimit = systemSettingRepository.findBySettingKey(SettingKey.TOTAL_DAILY_RESERVATION_LIMIT.getKey())
                .map(s -> Integer.parseInt(s.getSettingValue()))
                .orElse(SettingKey.TOTAL_DAILY_RESERVATION_LIMIT.getDefaultIntValue());

        // 2. 선택한 날짜의 전체 신청 현황 및 해당 세대 신청 현황 집계
        long targetDateTotalCount = reservationRepository.countAllDailyReservations(getStartOfDate(targetDate), getEndOfDate(targetDate));
        long targetDateUserCount = reservationRepository.countDailyReservations(household.getHouseholdId(), getStartOfDate(targetDate), getEndOfDate(targetDate));

        // 3. 현재 유효한 주차 정책 및 사용자 누적 통계 조회
        ReservationEventPolicy policy = reservationEventPolicyRepository.findActivePolicy(LocalDateTime.now())
                .orElseThrow(() -> new CustomException(ErrorCode.PARKING_POLICY_NOT_FOUND));

        long monthUsedCount = reservationRepository.countMonthlyReservations(household.getHouseholdId(), getStartOfMonth());

        var ACTIVE_STATUSES = List.of(com.example.demo.domain.shared.reservation.enums.Status.PENDING,
                com.example.demo.domain.shared.reservation.enums.Status.RESERVED);
        int currentActiveCount = (int) reservationRepository.countByHouseholdIdAndStatusIn(household.getHouseholdId(), ACTIVE_STATUSES);

        // 4. 화면에 노출할 안내 메시지 구성
        String systemMessage = (targetDateTotalCount >= totalDailyLimit) ? targetDate + "일은 이미 모든 예약이 마감되었습니다." : null;
        String warningMessage = "본 예약은 주차 공간을 확정적으로 보장하지 않으며, 현장 만차 시 입차가 제한될 수 있습니다.";
        if (targetDateTotalCount >= (totalDailyLimit * 0.8) && systemMessage == null) {
            warningMessage = "현재 해당 날짜의 예약량이 많아 주차가 혼잡할 수 있습니다. " + warningMessage;
        }

        return new ReservationEventPolicyResponseDto(
                policy.getEventName(), policy.getPermittedMinutes() != null ? policy.getPermittedMinutes() : 60,
                policy.getDailyLimitPerHousehold() != null ? policy.getDailyLimitPerHousehold() : 0,
                policy.getMonthlyLimitPerHousehold() != null ? policy.getMonthlyLimitPerHousehold() : 0,
                policy.getMaxActiveReservations(), totalDailyLimit, monthUsedCount, currentActiveCount,
                targetDateTotalCount, targetDateUserCount, policy.isNoShowPenaltyEnabled(), systemMessage, warningMessage
        );
    }

    /**
     * [내 방문 예약 내역 조회]
     * 로그인한 사용자의 모든 예약 내역을 최신순으로 조회합니다.
     */
    @Transactional(readOnly = true)
    public List<ReservationListResponseDto> getMyReservations(PrincipalDetails principalDetails) {
        User user = getValidatedUserAndHousehold(principalDetails.getUser().getUserId());
        return reservationRepository.findByUserOrderByCreatedAtDesc(user)
                .stream().map(ReservationListResponseDto::new).toList();
    }

    /**
     * [방문 예약 취소]
     * 예약 대기 또는 승인 상태인 예약을 취소 처리합니다.
     */
    @Transactional
    public ReservationCancelResponseDto cancelReservation(PrincipalDetails principalDetails, Long reservationId) {
        var PENDING = com.example.demo.domain.shared.reservation.enums.Status.PENDING;
        var RESERVED = com.example.demo.domain.shared.reservation.enums.Status.RESERVED;
        var CANCELLED = com.example.demo.domain.shared.reservation.enums.Status.CANCELLED;

        User user = getValidatedUserAndHousehold(principalDetails.getUser().getUserId());
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESERVATION_NOT_FOUND));

        // 1. 당일 취소 제한 (정책에 따라 하루 전까지만 가능)
        if (!reservation.getVisitStartAt().toLocalDate().isAfter(LocalDate.now())) {
            throw new CustomException(ErrorCode.CANCEL_NOT_TODAY);
        }

        // 2. 본인 소유 및 상태값 검증
        if (!reservation.getUser().getUserId().equals(user.getUserId())) {
            throw new CustomException(ErrorCode.RESERVATION_NOT_OWNER);
        }
        if (reservation.getStatus() == CANCELLED) {
            throw new CustomException(ErrorCode.RESERVATION_ALREADY_CANCELLED);
        }
        if (reservation.getStatus() != PENDING && reservation.getStatus() != RESERVED) {
            throw new CustomException(ErrorCode.RESERVATION_CANNOT_CANCEL_STATUS);
        }

        // 3. 실입차 여부 확인
        if (parkingLogRepository.isAlreadyInParkingLot(reservation.getCarNumber())) {
            throw new CustomException(ErrorCode.RESERVATION_ALREADY_USED);
        }

        // 4. 상태 업데이트 및 세대 활성 카운트 차감
        reservation.cancel(CANCELLED);
        householdRepository.decrementActiveReservationCount(user.getHousehold().getHouseholdId());

        // 5. 관련 결재 대기 건 자동 취소
        approvalRepository.findByTargetIdAndApprovalType(reservationId, ApprovalType.RESERVATION)
                .ifPresent(approval -> { if (approval.getStatus() == ApprovalStatus.PENDING) approval.updateStatus(ApprovalStatus.CANCELLED); });

        return new ReservationCancelResponseDto(reservation);
    }

    /**
     * [방문 예약 수정]
     * 기존 예약 내용을 변경합니다. 날짜 변경 시에는 정원 및 정책을 재검증합니다.
     */
    @Transactional
    public ReservationDetailResponseDto updateReservation(PrincipalDetails principalDetails, Long reservationId, ReservationApplyRequestDto reservationApplyRequestDto) {

        // 1. 기초 검증 (유저 활성화, 예약 존재 여부, 본인 소유 확인)
        User user = getValidatedUserAndHousehold(principalDetails.getUser().getUserId());
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESERVATION_NOT_FOUND));

        if (!reservation.getUser().getUserId().equals(user.getUserId())) {
            throw new CustomException(ErrorCode.RESERVATION_NOT_OWNER);
        }

        // 2. 수정 가능 상태 확인 (관리자 승인 전인 PENDING 상태만 수정 가능 기획)
        var PENDING = com.example.demo.domain.shared.reservation.enums.Status.PENDING;
        var RESERVED = com.example.demo.domain.shared.reservation.enums.Status.RESERVED;
        if (reservation.getStatus() != PENDING) {
            throw new CustomException(ErrorCode.RESERVATION_CANNOT_EDIT_STATUS);
        }

        String newCarNumber = reservationApplyRequestDto.getCarNumber();
        LocalDate newVisitDate = reservationApplyRequestDto.getVisitStartAt().toLocalDate();
        LocalDate oldVisitDate = reservation.getVisitStartAt().toLocalDate();
        LocalDateTime now = LocalDateTime.now();

        // 3. 차량 상태 공통 검증 (블랙리스트, 정기권, 입차 상태)
        if (vehicleBlacklistRepository.isCurrentlyBlacklisted(newCarNumber, now)) {
            throw new CustomException(ErrorCode.BLACKLIST_VEHICLE);
        }
        if (subscriptionRepository.hasActiveSubscription(newCarNumber, now)) {
            throw new CustomException(ErrorCode.ACTIVE_SUBSCRIPTION_EXISTS);
        }
        if (parkingLogRepository.isAlreadyInParkingLot(newCarNumber)) {
            throw new CustomException(ErrorCode.VEHICLE_ALREADY_ENTERED);
        }

        // 4. 중복 예약 검증 (현재 수정 중인 reservationId는 제외하고 카운트)
        if (reservationRepository.existsByCarNumberAndStatusInAndReservationIdNot(newCarNumber, List.of(PENDING, RESERVED), reservationId)) {
            throw new CustomException(ErrorCode.ALREADY_RESERVED_VEHICLE);
        }

        // 5. 주차 정책 조회
        ReservationEventPolicy policy = reservationEventPolicyRepository.findActivePolicy(now)
                .orElseThrow(() -> new CustomException(ErrorCode.PARKING_POLICY_NOT_FOUND));

        // 6. 날짜가 변경된 경우에만 정원 및 횟수 제한 재검증
        if (!newVisitDate.equals(oldVisitDate)) {
            // (6-1) 수정하려는 날짜가 오늘이나 과거면 안됨
            if (!newVisitDate.isAfter(LocalDate.now())) {
                throw new CustomException(ErrorCode.RESERVATION_NOT_TODAY);
            }

            // (6-2) 변경하려는 날짜의 아파트 전체 정원 확인
            validateSystemTotalLimit(newVisitDate);

            // (6-3) 변경하려는 날짜의 세대별 일일 제한 횟수 확인
            long visitDateCount = reservationRepository.countDailyReservations(
                    user.getHousehold().getHouseholdId(),
                    getStartOfDate(newVisitDate),
                    getEndOfDate(newVisitDate)
            );

            if (policy.getDailyLimitPerHousehold() != null && visitDateCount >= policy.getDailyLimitPerHousehold()) {
                throw new CustomException(ErrorCode.DAILY_LIMIT_EXCEEDED);
            }
        }

        // 7. 데이터 업데이트 (Dirty Checking에 의해 트랜잭션 종료 시 반영)
        int permittedMinutes = (policy.getPermittedMinutes() != null) ? policy.getPermittedMinutes() : 60;

        reservation.setCarNumber(newCarNumber);
        reservation.setPurpose(reservationApplyRequestDto.getPurpose());
        reservation.setVisitStartAt(reservationApplyRequestDto.getVisitStartAt());
        reservation.setVisitEndAt(reservationApplyRequestDto.getVisitStartAt().plusMinutes(permittedMinutes));

        return ReservationDetailResponseDto.fromEntity(reservation);
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