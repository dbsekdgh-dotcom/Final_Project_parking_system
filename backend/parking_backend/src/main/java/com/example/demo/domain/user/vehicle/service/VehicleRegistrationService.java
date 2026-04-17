package com.example.demo.domain.user.vehicle.service;

import com.example.demo.domain.shared.activityLog.ActivityLog;
import com.example.demo.domain.shared.activityLog.enums.ActivityType;
import com.example.demo.domain.shared.activityLog.repository.ActivityLogRepository;
import com.example.demo.domain.shared.approval.Approval;
import com.example.demo.domain.shared.approval.enums.ApprovalStatus;
import com.example.demo.domain.shared.approval.enums.ApprovalType;
import com.example.demo.domain.shared.approval.repository.ApprovalRepository;
import com.example.demo.domain.shared.parkinglog.repository.ParkingLogRepository;
import com.example.demo.domain.shared.subscription.repository.SubscriptionRepository;
import com.example.demo.domain.shared.systemSetting.SettingKey;
import com.example.demo.domain.shared.systemSetting.repository.SystemSettingRepository;
import com.example.demo.domain.shared.user.User;
import com.example.demo.domain.shared.user.UserRepository;
import com.example.demo.domain.shared.vehicle.Vehicle;
import com.example.demo.domain.shared.vehicle.VehicleRepository;
import com.example.demo.domain.shared.vehicle.enums.VehicleStatus;
import com.example.demo.domain.user.util.JaroWinklerMatcher;
import com.example.demo.domain.user.vehicle.dtos.request.VehicleCancelRequestDto;
import com.example.demo.domain.user.vehicle.dtos.request.VehicleRegistrationRequestDto;
import com.example.demo.domain.user.vehicle.dtos.response.VehicleResponseDto;
import com.example.demo.global.exception.CustomException;
import com.example.demo.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VehicleRegistrationService {

    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;
    private final SystemSettingRepository systemSettingRepository;
    private final ApprovalRepository approvalRepository;
    private final ActivityLogRepository activityLogRepository;
    private final JaroWinklerMatcher jaroWinklerMatcher;
    private final ParkingLogRepository parkingLogRepository;
    private final SubscriptionRepository subscriptionRepository;

    /**
     * 사용자 차량 등록 로직
     * OCR 유사도 검증을 통해 자동 승인(ACTIVE) 또는 관리자 대기(PENDING) 상태로 등록합니다.
     */
    @Transactional
    public void registerVehicle(Long userId, VehicleRegistrationRequestDto requestDto) {

        // [검증 1] 유저 존재 여부 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // [검증 2] 차량 중복 등록 체크
        Optional<Vehicle> existingVehicleOpt = vehicleRepository.findByCarNumber(requestDto.getCarNumber());

        if (existingVehicleOpt.isPresent()) {
            Vehicle existingVehicle = existingVehicleOpt.get();

            // 이미 사용 중이거나 관리자 승인 대기 중인 번호판이면 예외 발생
            if (existingVehicle.getStatus() == VehicleStatus.ACTIVE) {
                throw new CustomException(ErrorCode.DUPLICATE_VEHICLE);
            } else if (existingVehicle.getStatus() == VehicleStatus.PENDING) {
                throw new CustomException(ErrorCode.VEHICLE_ALREADY_PENDING);
            }
            // Tip: status가 DELETED인 경우 예외 없이 진행 (아래에서 데이터 업데이트/부활 처리)
        }

        // [검증 3] OCR 원본 데이터 유효성 확인
        if (requestDto.getOcrRawName() == null || requestDto.getOcrRawName().isBlank()) {
            throw new CustomException(ErrorCode.OCR_DATA_MISSING);
        }

        // [검증 4] OCR 인식 이름과 실제 유저 이름의 유사도 계산
        double similarityScore = jaroWinklerMatcher.match(requestDto.getOcrRawName(), user.getName());

        // [검증 5] 시스템 설정에 정의된 자동 승인 임계치 조회
        int threshold = systemSettingRepository.findBySettingKey(SettingKey.VEHICLE_AUTO_APPROVAL_THRESHOLD.getKey())
                .map(s -> Integer.parseInt(s.getSettingValue()))
                .orElseThrow(() -> new CustomException(ErrorCode.SYSTEM_SETTING_NOT_FOUND));

        // 유사도 기준 통과 여부에 따라 상태 결정
        VehicleStatus finalStatus = (similarityScore * 100 >= threshold) ? VehicleStatus.ACTIVE : VehicleStatus.PENDING;

        // [데이터 처리] Vehicle 정보 저장 (신규 생성 또는 기존 데이터 부활)
        Vehicle vehicle = existingVehicleOpt.map(v -> {
            v.updateRegistration(user, requestDto.getVehicleName(), finalStatus);
            return vehicleRepository.save(v);
        }).orElseGet(() -> {
            Vehicle newVehicle = Vehicle.builder()
                    .user(user)
                    .vehicleName(requestDto.getVehicleName())
                    .carNumber(requestDto.getCarNumber())
                    .status(finalStatus)
                    .build();
            return vehicleRepository.save(newVehicle);
        });

        // [결재 처리] Approval 엔티티 생성 및 상태 기록
        ApprovalStatus approvalStatus = (finalStatus == VehicleStatus.ACTIVE) ? ApprovalStatus.APPROVED : ApprovalStatus.PENDING;
        Approval approval = Approval.builder()
                .approvalType(ApprovalType.VEHICLE)
                .targetId(vehicle.getId())
                .requestUserId(user)
                .status(approvalStatus)
                .processedAt(finalStatus == VehicleStatus.ACTIVE ? LocalDateTime.now() : null)
                .build();
        approvalRepository.save(approval);

        // [로그 기록] 차량 등록 활동 기록
        ActivityLog activityLog = ActivityLog.builder()
                .activityType(ActivityType.VEHICLE_REGISTERED)
                .carNumber(vehicle.getCarNumber())
                .household(user.getHousehold())
                .message(String.format("차량 등록: %s (유사도: %.1f%%, 상태: %s)",
                        vehicle.getCarNumber(), similarityScore * 100, finalStatus))
                .build();
        activityLogRepository.save(activityLog);
    }

    /**
     * 내 차량 조회
     */
    public VehicleResponseDto getMyVehicle(Long userId) {
        return vehicleRepository.findCurrentVehicle(userId)
                .map(vehicle -> VehicleResponseDto.builder()
                        .vehicleId(vehicle.getId())
                        .carNumber(vehicle.getCarNumber())
                        .vehicleName(vehicle.getVehicleName())
                        .status(vehicle.getStatus())
                        .createdAt(vehicle.getCreatedAt())
                        .build())
                .orElse(null);
    }

    /**
     * 승인 대기 중인 차량 등록 신청 취소 로직
     */
    @Transactional
    public void cancelVehicleRegistration(Long userId, VehicleCancelRequestDto requestDto) {

        // [검증 1] 대상 차량 존재 확인
        Vehicle vehicle = vehicleRepository.findById(requestDto.getVehicleId())
                .orElseThrow(() -> new CustomException(ErrorCode.VEHICLE_NOT_FOUND));

        // [검증 2] 소유권 확인 (본인이 신청한 차량인지)
        if (vehicle.getUser() == null || !vehicle.getUser().getUserId().equals(userId)) {
            throw new CustomException(ErrorCode.ACCESS_DENIED_VEHICLE);
        }

        // [검증 3] 상태 확인 (오직 PENDING 상태일 때만 취소 가능)
        if (vehicle.getStatus() != VehicleStatus.PENDING) {
            throw new CustomException(ErrorCode.INVALID_VEHICLE_STATUS);
        }

        // [데이터 처리] 대기 중인 결재 건 조회 및 취소 처리
        Approval approval = approvalRepository.findByTargetIdAndApprovalType(vehicle.getId(), ApprovalType.VEHICLE)
                .filter(a -> a.getStatus() == ApprovalStatus.PENDING)
                .orElseThrow(() -> new CustomException(ErrorCode.APPROVAL_NOT_FOUND));

        approval.cancel();           // 결재 상태 -> CANCELLED
        vehicle.softDelete();        // 차량 상태 -> DELETED
        vehicle.assignUser(null);    // 유저와의 연관관계 해제
    }

    /**
     * 활성화된 차량 삭제 로직 (물리적 삭제가 아닌 상태 변경)
     */
    @Transactional
    public void deleteVehicle(Long userId, Long vehicleId) {

        // [검증 1] 유저 존재 여부 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // [검증 2] 삭제 대상 차량 존재 확인
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new CustomException(ErrorCode.VEHICLE_NOT_FOUND));

        // [검증 3] 본인 소유 차량 여부 확인
        if (vehicle.getUser() == null || !vehicle.getUser().getUserId().equals(userId)) {
            throw new CustomException(ErrorCode.ACCESS_DENIED_VEHICLE);
        }

        // [검증 4] 주차 현황 확인
        // - 입주민/정기권 혜택을 받으며 입차 중(ENTERED 등)인 경우 삭제 불가
        if (parkingLogRepository.existsActiveBenefitLogByVehicleId(vehicleId)) {
            throw new CustomException(ErrorCode.CANNOT_DELETE_VEHICLE_IN_PARKING);
        }

        // [검증 5] 정기권 보유 여부 확인
        // - 현재 사용 중이거나 미래에 시작될 유효한 정기권이 있다면 삭제 불가
        if (subscriptionRepository.hasActiveOrFutureSubscription(vehicleId, LocalDateTime.now())) {
            throw new CustomException(ErrorCode.CANNOT_DELETE_VEHICLE_WITH_SUBSCRIPTION);
        }

        // [데이터 처리] Soft Delete 실행 및 소유권 해제
        vehicle.softDelete();        // VehicleStatus.DELETED 로 변경
        vehicle.assignUser(null);    // 번호판 재사용을 위해 user_id 연관관계 끊기
    }
}