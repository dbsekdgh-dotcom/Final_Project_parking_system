package com.example.demo.domain.auth.user.service;

import com.example.demo.domain.approval.enums.ApprovalStatus;
import com.example.demo.domain.approval.repository.ApprovalRepository;
import com.example.demo.domain.notification.repository.NotificationRepository;
import com.example.demo.domain.parking.log.repository.ParkingLogRepository;
import com.example.demo.domain.payment.point.repository.UserPointRepository;
import com.example.demo.domain.payment.subscription.repository.SubscriptionRepository;
import com.example.demo.domain.report.entity.ReportStatus;
import com.example.demo.domain.report.repository.ReportRepository;
import com.example.demo.domain.reservation.enums.Status;
import com.example.demo.domain.reservation.repository.ReservationRepository;
import com.example.demo.domain.resident.User;
import com.example.demo.domain.resident.household.Household;
import com.example.demo.domain.resident.household.enums.IsActive;
import com.example.demo.domain.resident.household.repository.HouseholdRepository;
import com.example.demo.domain.auth.user.constants.UserAuthConstants;
import com.example.demo.domain.auth.user.dtos.request.UserWithdrawRequestDto;
import com.example.demo.domain.auth.user.repository.SocialAccountRepository;
import com.example.demo.domain.auth.user.repository.UserAuthRepository;
import com.example.demo.domain.vehicle.Vehicle;
import com.example.demo.domain.vehicle.VehicleRepository;
import com.example.demo.domain.vehicle.enums.VehicleStatus;
import com.example.demo.global.exception.AuthException;
import com.example.demo.global.exception.BusinessException;
import com.example.demo.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserWithdrawService {

    private final UserAuthRepository        userAuthRepository;
    private final SocialAccountRepository   socialAccountRepository;
    private final PasswordEncoder           passwordEncoder;
    private final UserVerificationService   userVerificationService;
    private final VehicleRepository         vehicleRepository;
    private final SubscriptionRepository    subscriptionRepository;
    private final ParkingLogRepository      parkingLogRepository;
    private final ReservationRepository     reservationRepository;
    private final HouseholdRepository       householdRepository;
    private final ApprovalRepository        approvalRepository;
    private final ReportRepository          reportRepository;
    private final UserPointRepository       userPointRepository;
    private final NotificationRepository    notificationRepository;

    @Transactional
    public void withdraw(String email, UserWithdrawRequestDto dto) {

        User user = userAuthRepository.findByEmail(email)
                .orElseThrow(() -> new AuthException(ErrorCode.USER_NOT_FOUND));

        log.info("회원 탈퇴 처리 시작 - 이메일: {}", email);

        if (user.getStatus() == com.example.demo.domain.resident.enums.Status.DELETED) {
            throw new AuthException(ErrorCode.WITHDRAWN_ACCOUNT);
        }

        if (!UserAuthConstants.WITHDRAW_CONFIRM_TEXT.equals(dto.getConfirmText())) {
            throw new AuthException(ErrorCode.INVALID_CONFIRM_TEXT);
        }

        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new AuthException(ErrorCode.PASSWORD_MISMATCH);
        }

        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            throw new AuthException(ErrorCode.LOGIN_FAILED);
        }

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new AuthException(ErrorCode.WITHDRAW_PASSWORD_MISMATCH);
        }

        // ── 1. 차단 조건 체크 (정기권 / 입차중) ───────────────────────────
        checkBlockingConditions(user);

        // ── 2. 연관 데이터 정리 ────────────────────────────────────────────
        cancelPendingReservations(user);
        cancelPendingApprovals(user);
        cancelPendingReports(user);
        deactivateHousehold(user);
        resetPoint(user);
        softDeleteNotifications(user);

        // ── 3. 유저 탈퇴 처리 ─────────────────────────────────────────────
        user.withdraw();
        userAuthRepository.saveAndFlush(user);

        try {
            socialAccountRepository.deleteByUserId(user.getUserId());
        } catch (Exception e) {
            log.error("소셜 계정 정보 삭제 중 오류 발생 (ID: {}): ", user.getUserId(), e);
            throw new AuthException(ErrorCode.SOCIAL_LINK_FAILED);
        }

        userVerificationService.deleteRefreshToken(email);

        log.info("회원 탈퇴 성공 - 이메일: {}", user.getEmail());
    }

    // 정기권 ACTIVE 또는 입차·출차대기 중이면 탈퇴 차단
    private void checkBlockingConditions(User user) {
        List<Vehicle> activeVehicles = vehicleRepository
                .findByUser_UserIdAndStatus(user.getUserId(), VehicleStatus.ACTIVE);

        boolean hasActiveSubscription = activeVehicles.stream()
                .anyMatch(v -> subscriptionRepository
                        .hasActiveOrFutureSubscription(v.getId(), LocalDateTime.now()));
        if (hasActiveSubscription) {
            throw new BusinessException(ErrorCode.WITHDRAW_BLOCKED_ACTIVE_SUBSCRIPTION);
        }

        if (!activeVehicles.isEmpty()) {
            List<String> carNumbers = activeVehicles.stream()
                    .map(Vehicle::getCarNumber)
                    .toList();
            if (parkingLogRepository.existsActiveByCarNumbers(carNumbers)) {
                throw new BusinessException(ErrorCode.WITHDRAW_BLOCKED_VEHICLE_IN_PARKING);
            }
        }
    }

    // 방문예약 PENDING / RESERVED → CANCELLED, household 카운트 감소
    private void cancelPendingReservations(User user) {
        List<Status> targets = List.of(Status.PENDING, Status.RESERVED);
        var reservations = reservationRepository
                .findByUserIdAndStatusIn(user.getUserId(), targets);

        Household household = user.getHousehold();
        for (var reservation : reservations) {
            reservation.cancel(Status.CANCELLED);
            if (household != null) {
                householdRepository.decrementActiveReservationCount(household.getHouseholdId());
            }
        }
    }

    // 승인 대기 중인 Approval → CANCELLED
    private void cancelPendingApprovals(User user) {
        approvalRepository
                .findByRequestUserId_UserIdAndStatus(user.getUserId(), ApprovalStatus.PENDING)
                .forEach(a -> a.cancel());
    }

    // 신고 PENDING → CANCELLED
    private void cancelPendingReports(User user) {
        reportRepository
                .findByReporterIdAndStatus(user.getUserId(), ReportStatus.PENDING)
                .forEach(r -> r.cancel());
    }

    // Household INACTIVE + 카운트 초기화, user.household 연결 해제
    private void deactivateHousehold(User user) {
        Household household = user.getHousehold();
        if (household != null && household.getIsActive() == IsActive.ACTIVE) {
            household.deactivate();
            user.setHousehold(null);
        }
    }

    // 포인트 0 초기화
    private void resetPoint(User user) {
        userPointRepository.resetPoint(user.getUserId());
    }

    // 알림 소프트 삭제 (deletedAt = now)
    private void softDeleteNotifications(User user) {
        notificationRepository.softDeleteByUserId(user.getUserId(), LocalDateTime.now());
    }
}
