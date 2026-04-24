package com.example.demo.domain.resident.apply.service;

import com.example.demo.domain.system.activitylog.ActivityLog;
import com.example.demo.domain.system.activitylog.enums.ActivityType;
import com.example.demo.domain.system.activitylog.repository.ActivityLogRepository;
import com.example.demo.domain.approval.Approval;
import com.example.demo.domain.approval.enums.ApprovalStatus;
import com.example.demo.domain.approval.enums.ApprovalType;
import com.example.demo.domain.approval.repository.ApprovalRepository;
import com.example.demo.domain.parking.log.repository.ParkingLogRepository;
import com.example.demo.domain.resident.household.Household;
import com.example.demo.domain.resident.household.enums.IsActive;
import com.example.demo.domain.resident.household.repository.HouseholdRepository;
import com.example.demo.domain.reservation.repository.ReservationRepository;
import com.example.demo.domain.resident.User;
import com.example.demo.domain.resident.UserRepository;
import com.example.demo.domain.resident.enums.Status;
import com.example.demo.domain.vehicle.VehicleRepository;
import com.example.demo.domain.vehicle.enums.VehicleStatus;
import com.example.demo.domain.resident.apply.dtos.request.ResidentApplyRequestDto;
import com.example.demo.domain.resident.apply.dtos.response.ResidentApplyCancelResponseDto;
import com.example.demo.domain.resident.apply.dtos.response.ResidentApplyResponseDto;
import com.example.demo.domain.resident.apply.dtos.response.UserStatusResponseDto;
import com.example.demo.global.exception.CustomException;
import com.example.demo.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ResidentApplyService {

    private final ApprovalRepository approvalRepository;
    private final HouseholdRepository householdRepository;
    private final UserRepository userRepository;
    private final ActivityLogRepository activityLogRepository;
    private final ReservationRepository reservationRepository;
    private final VehicleRepository vehicleRepository;
    private final ParkingLogRepository parkingLogRepository;

    /**
     * [입주 신청 등록]
     */
    @Transactional
    public ResidentApplyResponseDto apply(Long userId, ResidentApplyRequestDto residentApplyRequestDto) {
        // [검증] 유저 최신 정보 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (user.getStatus() != Status.ACTIVE) {
            throw new CustomException(ErrorCode.ACCOUNT_DISABLED);
        }

        // [검증] 이미 입주 상태이거나 대기 중인 신청이 있는지 확인
        if (user.getHousehold() != null) {
            throw new CustomException(ErrorCode.ALREADY_RESIDENT);
        }

        if (approvalRepository.existsByRequestUserIdAndApprovalTypeAndStatus(user, ApprovalType.RESIDENT, ApprovalStatus.PENDING)) {
            throw new CustomException(ErrorCode.ALREADY_APPLIED_RESIDENT);
        }

        // [검증] 신청하려는 세대 존재 여부 및 활성화(이미 다른 사람 입주) 여부 확인
        var household = householdRepository.findById(residentApplyRequestDto.getHouseholdId())
                .orElseThrow(() -> new CustomException(ErrorCode.HOUSEHOLD_NOT_FOUND));

        if (household.getIsActive() == IsActive.ACTIVE) {
            throw new CustomException(ErrorCode.HOUSEHOLD_ALREADY_ACTIVE);
        }

        // [검증] 해당 세대에 대해 이미 다른 사람이 보낸 대기 중인 승인 요청이 있는지 확인
        boolean isUnitAlreadyPending = approvalRepository.existsByTargetIdAndApprovalTypeAndStatus(
                household.getHouseholdId(), ApprovalType.RESIDENT, ApprovalStatus.PENDING);

        if (isUnitAlreadyPending) {
            throw new CustomException(ErrorCode.NOT_AVAILABLE_HOUSEHOLD);
        }

        // 활동 로그 남기기
        activityLogRepository.save(ActivityLog.builder()
                .activityType(ActivityType.RESIDENT_REGISTERED)
                .user(user)
                .household(household)
                .message(String.format("입주민 신청 접수 : %d호 (신청자: %s)",
                        household.getUnitNo(), user.getName()))
                .build());

        // 결재 테이블(Approval)에 신청 내역 저장
        Approval approval = Approval.builder()
                .approvalType(ApprovalType.RESIDENT)
                .targetId(household.getHouseholdId())
                .requestUserId(user)
                .status(ApprovalStatus.PENDING)
                .build();

        Approval savedApproval = approvalRepository.save(approval);
        return new ResidentApplyResponseDto(savedApproval);
    }

    /**
     * [입주 신청 취소]
     */
    @Transactional
    public ResidentApplyCancelResponseDto cancel(Long userId, Long approvalId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 본인이 보낸 입주 신청(RESIDENT)이면서 대기(PENDING) 상태인 것만 조회
        Approval approval = approvalRepository.findByApprovalIdAndRequestUserId_UserId(approvalId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.APPLY_NOT_FOUND));

        if (approval.getApprovalType() != ApprovalType.RESIDENT) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        if (approval.getStatus() != ApprovalStatus.PENDING) {
            throw new CustomException(ErrorCode.CANNOT_CANCEL_APPROVED);
        }

        // 취소 상태로 변경 (더티 체킹)
        approval.setStatus(ApprovalStatus.CANCELLED);
        return new ResidentApplyCancelResponseDto(approval);
    }

    /**
     * [퇴거]
     */
    @Transactional
    public void leave(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (user.getHousehold() == null) {
            throw new CustomException(ErrorCode.NOT_RESIDENT_USER);
        }

        // 입차 중인 차량 있으면 차단
        List<String> carNumbers = vehicleRepository
                .findByUser_UserIdAndStatus(userId, VehicleStatus.ACTIVE)
                .stream()
                .map(v -> v.getCarNumber())
                .toList();

        if (carNumbers.stream().anyMatch(parkingLogRepository::isAlreadyInParkingLot)) {
            throw new CustomException(ErrorCode.CANNOT_LEAVE_VEHICLE_IN_PARKING);
        }

        // 방문 예약 자동 취소
        reservationRepository
                .findByUserIdAndStatusIn(userId, List.of(
                        com.example.demo.domain.reservation.enums.Status.PENDING,
                        com.example.demo.domain.reservation.enums.Status.RESERVED))
                .forEach(r -> {
                    r.cancel(com.example.demo.domain.reservation.enums.Status.CANCELLED);
                    householdRepository.decrementActiveReservationCount(user.getHousehold().getHouseholdId());
                });

        // 세대 해제
        Household household = user.getHousehold();
        household.deactivate();
        user.setHousehold(null);
    }

    /**
     * [마이페이지용 상태 조회] - 입주 완료, 대기 중, 미신청 상태 구분
     */
    @Transactional(readOnly = true)
    public UserStatusResponseDto getUserStatus(Long userId) {

        // 윤진추가: userId가 null이면 바로 "NONE"상태로 돌려보내기
        if(userId ==null){
            return UserStatusResponseDto.builder()
                    .userStatus("NONE")
                    .activeApprovalId(null)
                    .build();
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 현재 진행 중인(PENDING) 입주 신청 건이 있는지 확인
        Long activeId = approvalRepository.findTopByRequestUserIdAndApprovalTypeAndStatusOrderByCreatedAtDesc(
                        user, ApprovalType.RESIDENT, ApprovalStatus.PENDING)
                .map(Approval::getApprovalId)
                .orElse(null);

        String statusCode;
        if (user.getHousehold() != null) {
            statusCode = "RESIDENT"; // 실제 세대에 배정된 경우
        } else if (activeId != null) {
            statusCode = "PENDING";  // 신청 후 승인 기다리는 경우
        } else {
            statusCode = "NONE";     // 아무것도 해당 안 되는 경우
        }

        return UserStatusResponseDto.builder()
                .userStatus(statusCode)
                .activeApprovalId(activeId)
                .build();
    }

}