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
import com.example.demo.domain.shared.user.User;
import com.example.demo.domain.shared.user.UserRepository;
import com.example.demo.domain.shared.user.enums.Status;
import com.example.demo.domain.shared.vehicleblacklist.repository.VehicleBlacklistRepository;
import com.example.demo.domain.user.auth.principal.PrincipalDetails;
import com.example.demo.domain.user.reservaion.dtos.request.ReservationApplyRequestDto;
import com.example.demo.domain.user.reservaion.dtos.response.ReservationCancelResponseDto;
import com.example.demo.domain.user.reservaion.dtos.response.ReservationDetailResponseDto;
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

/**
 * 입주민용 방문 예약 관련 비즈니스 로직을 처리하는 서비스입니다.
 */
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

    /**
     * [방문 예약 신청]
     * 차량 번호, 방문 시간 등을 입력받아 예약을 생성하고 관리자 결재를 요청합니다.
     */
    @Transactional
    public ReservationDetailResponseDto applyReservation(PrincipalDetails principalDetails, ReservationApplyRequestDto reservationApplyRequestDto) {

        // 1. 유저 정보 조회 및 활성화 상태 검증
        User user = userRepository.findById(principalDetails.getUser().getUserId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if(user.getStatus() != Status.ACTIVE) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }

        // 2. 세대 소속 여부 및 세대 활성화 상태 확인
        Household household = user.getHousehold();
        if(household == null || household.getIsActive() != IsActive.ACTIVE) {
            throw new CustomException(ErrorCode.NOT_AVAILABLE_HOUSEHOLD);
        }

        String carNumber = reservationApplyRequestDto.getCarNumber();
        LocalDateTime now = LocalDateTime.now();

        // 3. 블랙리스트 차량 여부 확인
        if(vehicleBlacklistRepository.isCurrentlyBlacklisted(carNumber, now)) {
            throw new CustomException(ErrorCode.BLACKLIST_VEHICLE);
        }

        // 4. 이미 정기권이 등록된 차량인지 확인
        if(subscriptionRepository.hasActiveSubscription(carNumber, now)) {
            throw new CustomException(ErrorCode.ACTIVE_SUBSCRIPTION_EXISTS);
        }

        // 5. 현재 주차장에 이미 입차되어 있는 차량인지 확인
        if(parkingLogRepository.isAlreadyInParkingLot(carNumber)) {
            throw new CustomException(ErrorCode.VEHICLE_ALREADY_ENTERED);
        }

        // 6. 중복 예약 확인 (PENDING 또는 RESERVED 상태의 예약이 있는지)
        var PENDING = com.example.demo.domain.shared.reservation.enums.Status.PENDING;
        var RESERVED = com.example.demo.domain.shared.reservation.enums.Status.RESERVED;

        if(reservationRepository.existsByCarNumberAndStatusIn(carNumber, List.of(PENDING, RESERVED))) {
            throw new CustomException(ErrorCode.ALREADY_RESERVED_VEHICLE);
        }

        // 7. 주차 정책 및 세대별 예약 제한 확인
        ReservationEventPolicy policy = reservationEventPolicyRepository.findActivePolicy(now)
                .orElseThrow(() -> new CustomException(ErrorCode.PARKING_POLICY_NOT_FOUND));

        // 세대별 동시 활성 예약 수 제한 체크
        if(household.getActiveReservationCount() >= policy.getMaxActiveReservations()) {
            throw new CustomException(ErrorCode.MAX_RESERVATION_EXCEEDED);
        }

        // 세대별 일일 예약 횟수 제한 체크
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);
        long todayCount = reservationRepository.countDailyReservations(household.getHouseholdId(), startOfDay, endOfDay);

        if(policy.getDailyLimitPerHousehold() != null && todayCount >= policy.getDailyLimitPerHousehold()) {
            throw new CustomException(ErrorCode.DAILY_LIMIT_EXCEEDED);
        }

        // 8. 정책에 따른 방문 종료 시간 자동 계산
        LocalDateTime visitStartAt = reservationApplyRequestDto.getVisitStartAt();
        int minutesToAdd = (policy.getPermittedMinutes() != null) ? policy.getPermittedMinutes() : 60;
        LocalDateTime visitEndAt = visitStartAt.plusMinutes(minutesToAdd);

        // 9. 예약(Reservation) 엔티티 생성 및 저장
        Reservation reservation = Reservation.builder()
                .user(user)
                .carNumber(carNumber)
                .purpose(reservationApplyRequestDto.getPurpose())
                .visitStartAt(visitStartAt)
                .visitEndAt(visitEndAt)
                .status(PENDING)
                .isFree(true)
                .build();

        Reservation savedReservation = reservationRepository.save(reservation);

        // 10. 관리자 승인을 위한 결재 데이터 생성
        Approval approval = Approval.builder()
                .approvalType(ApprovalType.RESERVATION)
                .targetId(savedReservation.getReservationId())
                .requestUserId(user)
                .status(ApprovalStatus.PENDING)
                .build();

        approvalRepository.save(approval);

        // 11. 활동 로그 기록
        activityLogRepository.save(ActivityLog.builder()
                .activityType(ActivityType.RESERVATION_CREATED)
                .reservation(savedReservation)
                .carNumber(carNumber)
                .household(household)
                .message(String.format("[%s] 차량 방문 예약 신청 (세대: %s)", carNumber, household.getUnitNo()))
                .build());

        // 12. 세대 활성 예약 카운트 증가
        householdRepository.incrementActiveReservationCount(household.getHouseholdId());

        return ReservationDetailResponseDto.fromEntity(savedReservation);
    }

    /**
     * [내 방문 예약 내역 조회]
     */
    @Transactional(readOnly = true)
    public List<ReservationListResponseDto> getMyReservations(PrincipalDetails principalDetails) {

        User user = userRepository.findById(principalDetails.getUser().getUserId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (user.getHousehold() == null) {
            throw new CustomException(ErrorCode.NOT_RESIDENT_USER);
        }

        return reservationRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(ReservationListResponseDto::new)
                .toList();
    }

    /**
     * [방문 예약 취소]
     * 입차 여부 및 예약 상태를 확인하여 취소 처리를 수행합니다.
     */
    @Transactional
    public ReservationCancelResponseDto cancelReservation(PrincipalDetails principalDetails, Long reservationId) {

        // 상태값 상수화
        var PENDING = com.example.demo.domain.shared.reservation.enums.Status.PENDING;
        var RESERVED = com.example.demo.domain.shared.reservation.enums.Status.RESERVED;
        var CANCELLED = com.example.demo.domain.shared.reservation.enums.Status.CANCELLED;

        var APP_PENDING = ApprovalStatus.PENDING;
        var APP_CANCELLED = ApprovalStatus.CANCELLED;

        // 1. 데이터 조회
        User user = userRepository.findById(principalDetails.getUser().getUserId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESERVATION_NOT_FOUND));

        // 2. 권한 검증
        if (!reservation.getUser().getUserId().equals(user.getUserId())) {
            throw new CustomException(ErrorCode.RESERVATION_NOT_OWNER);
        }

        // 3. 상태 검증
        var currentStatus = reservation.getStatus();

        // (1) 이미 취소된 경우
        if (currentStatus == CANCELLED) {
            throw new CustomException(ErrorCode.RESERVATION_ALREADY_CANCELLED);
        }

        // (2) PENDING/RESERVED 외 상태(완료 등) 체크
        if (currentStatus != PENDING && currentStatus != RESERVED) {
            throw new CustomException(ErrorCode.RESERVATION_CANNOT_CANCEL_STATUS);
        }

        // (3) 🚩 [추가 검증] 실제 주차장에 이미 입차했는지 확인 (실물 입차 시 취소 불가)
        if (parkingLogRepository.isAlreadyInParkingLot(reservation.getCarNumber())) {
            throw new CustomException(ErrorCode.RESERVATION_ALREADY_USED);
        }

        // 4. 취소 로직 실행
        // (1) 엔티티 취소 처리
        reservation.cancel(CANCELLED);

        // (2) 세대 활성 카운트 차감
        if (user.getHousehold() != null) {
            householdRepository.decrementActiveReservationCount(user.getHousehold().getHouseholdId());
        }

        // (3) 결재 대기 건 자동 취소
        approvalRepository.findByTargetIdAndApprovalType(reservationId, ApprovalType.RESERVATION)
                .ifPresent(approval -> {
                    if (approval.getStatus() == APP_PENDING) {
                        approval.updateStatus(APP_CANCELLED);
                    }
                });

        // (4) 활동 로그 저장
        activityLogRepository.save(ActivityLog.builder()
                .activityType(ActivityType.RESERVATION_CANCELLED)
                .reservation(reservation)
                .carNumber(reservation.getCarNumber())
                .household(user.getHousehold())
                .message(String.format("[%s] 방문 예약 취소 (사용자 직접 취소)", reservation.getCarNumber()))
                .build());

        return new ReservationCancelResponseDto(reservation);
    }
}