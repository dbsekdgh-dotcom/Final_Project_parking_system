package com.example.demo.domain.user.vehicle.service;

import com.example.demo.domain.shared.activityLog.ActivityLog;
import com.example.demo.domain.shared.activityLog.enums.ActivityType;
import com.example.demo.domain.shared.activityLog.repository.ActivityLogRepository;
import com.example.demo.domain.shared.approval.Approval;
import com.example.demo.domain.shared.approval.enums.ApprovalStatus;
import com.example.demo.domain.shared.approval.enums.ApprovalType;
import com.example.demo.domain.shared.approval.repository.ApprovalRepository;
import com.example.demo.domain.shared.systemSetting.SettingKey;
import com.example.demo.domain.shared.systemSetting.repository.SystemSettingRepository;
import com.example.demo.domain.shared.user.User;
import com.example.demo.domain.shared.user.UserRepository;
import com.example.demo.domain.shared.vehicle.Vehicle;
import com.example.demo.domain.shared.vehicle.VehicleRepository;
import com.example.demo.domain.shared.vehicle.enums.VehicleStatus;
import com.example.demo.domain.user.util.JaroWinklerMatcher;
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
@Transactional
public class VehicleRegistrationService {

    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;
    private final SystemSettingRepository systemSettingRepository;
    private final ApprovalRepository approvalRepository;
    private final ActivityLogRepository activityLogRepository;
    private final JaroWinklerMatcher jaroWinklerMatcher;

    /**
     * 사용자 차량 등록 로직
     * OCR 유사도 검증을 통해 자동 승인(ACTIVE) 또는 관리자 대기(PENDING) 상태로 등록합니다.
     */
    public void registerVehicle(Long userId, VehicleRegistrationRequestDto requestDto) {

        // 1. 유저 존재 여부 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 2. 차량 중복 체크 및 상태 확인
        Optional<Vehicle> existingVehicleOpt = vehicleRepository.findByCarNumber(requestDto.getCarNumber());

        if (existingVehicleOpt.isPresent()) {
            Vehicle existingVehicle = existingVehicleOpt.get();

            // 이미 사용 중이거나 승인 대기 중인 번호판인 경우 예외 발생
            if (existingVehicle.getStatus() == VehicleStatus.ACTIVE) {
                throw new CustomException(ErrorCode.DUPLICATE_VEHICLE);
            } else if (existingVehicle.getStatus() == VehicleStatus.PENDING) {
                throw new CustomException(ErrorCode.VEHICLE_ALREADY_PENDING);
            }
            // Tip: status가 DELETED인 경우 예외를 던지지 않고 아래 '부활' 로직으로 진행됨
        }

        // 3. OCR 데이터 검증 및 유사도 계산
        if (requestDto.getOcrRawName() == null || requestDto.getOcrRawName().isBlank()) {
            throw new CustomException(ErrorCode.OCR_DATA_MISSING);
        }

        // DB에 저장된 실제 사용자 이름과 OCR로 읽어온 이름 비교
        double similarityScore = jaroWinklerMatcher.match(requestDto.getOcrRawName(), user.getName());

        // DB에서 자동 승인 임계치(Threshold) 조회 (기본값 보통 95)
        int threshold = systemSettingRepository.findBySettingKey(SettingKey.VEHICLE_AUTO_APPROVAL_THRESHOLD.getKey())
                .map(s -> Integer.parseInt(s.getSettingValue()))
                .orElseThrow(() -> new CustomException(ErrorCode.SYSTEM_SETTING_NOT_FOUND));

        // 유사도가 기준치 이상이면 즉시 활성화(ACTIVE), 미달이면 승인 대기(PENDING)
        VehicleStatus finalStatus = (similarityScore * 100 >= threshold) ? VehicleStatus.ACTIVE : VehicleStatus.PENDING;

        // 4. Vehicle 데이터 저장 (기존 데이터 재사용 또는 신규 생성)
        Vehicle vehicle = existingVehicleOpt.map(v -> {
            // [부활] 소프트 딜리트된 차량 정보가 있다면 현재 유저 정보로 덮어쓰기
            v.updateRegistration(user, requestDto.getVehicleName(), finalStatus);
            return vehicleRepository.save(v);
        }).orElseGet(() -> {
            // [신규] 처음 등록되는 번호판인 경우 신규 생성
            Vehicle newVehicle = Vehicle.builder()
                    .user(user)
                    .vehicleName(requestDto.getVehicleName())
                    .carNumber(requestDto.getCarNumber())
                    .status(finalStatus)
                    .build();
            return vehicleRepository.save(newVehicle);
        });

        // 5. Approval(결재) 이력 생성
        ApprovalStatus approvalStatus = (finalStatus == VehicleStatus.ACTIVE) ? ApprovalStatus.APPROVED : ApprovalStatus.PENDING;

        Approval approval = Approval.builder()
                .approvalType(ApprovalType.VEHICLE)
                .targetId(vehicle.getId()) // Vehicle의 PK(id) 저장
                .requestUserId(user)
                .status(approvalStatus)
                // 자동 승인된 경우 현재 시간을 '처리 시간'으로 기록 (관리자 ID는 자동이므로 생략)
                .processedAt(finalStatus == VehicleStatus.ACTIVE ? LocalDateTime.now() : null)
                .build();

        approvalRepository.save(approval);

        // 6. 활동 로그(ActivityLog) 기록
        ActivityLog activityLog = ActivityLog.builder()
                .activityType(ActivityType.VEHICLE_REGISTERED)
                .carNumber(vehicle.getCarNumber())
                .household(user.getHousehold()) // 유저의 세대 정보가 있으면 저장, 없으면 null
                .message(String.format("차량 등록: %s (유사도: %.1f%%, 상태: %s)",
                        vehicle.getCarNumber(), similarityScore * 100, finalStatus))
                .build();

        activityLogRepository.save(activityLog);
    }


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
}