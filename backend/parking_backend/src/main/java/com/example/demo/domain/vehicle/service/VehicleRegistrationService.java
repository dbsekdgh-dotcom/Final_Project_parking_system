package com.example.demo.domain.vehicle.service;

import com.example.demo.domain.system.activitylog.ActivityLog;
import com.example.demo.domain.system.activitylog.enums.ActivityType;
import com.example.demo.domain.system.activitylog.repository.ActivityLogRepository;
import com.example.demo.domain.approval.Approval;
import com.example.demo.domain.approval.enums.ApprovalStatus;
import com.example.demo.domain.approval.enums.ApprovalType;
import com.example.demo.domain.approval.repository.ApprovalRepository;
import com.example.demo.domain.parking.log.repository.ParkingLogRepository;
import com.example.demo.domain.payment.subscription.repository.SubscriptionRepository;
import com.example.demo.domain.system.setting.SettingKey;
import com.example.demo.domain.system.setting.repository.SystemSettingRepository;
import com.example.demo.domain.resident.User;
import com.example.demo.domain.resident.UserRepository;
import com.example.demo.domain.resident.enums.Status;
import com.example.demo.domain.vehicle.Vehicle;
import com.example.demo.domain.vehicle.VehicleRepository;
import com.example.demo.domain.vehicle.enums.VehicleStatus;
import com.example.demo.global.util.JaroWinklerMatcher;
import com.example.demo.domain.vehicle.dtos.request.VehicleCancelRequestDto;
import com.example.demo.domain.vehicle.dtos.request.VehicleRegistrationRequestDto;
import com.example.demo.domain.vehicle.dtos.response.VehicleResponseDto;
import com.example.demo.global.exception.CustomException;
import com.example.demo.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
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
     * [차량 등록 로직]
     * OCR 원본 데이터와 사용자의 입력값, 그리고 DB의 유저 정보를 대조하여
     * 자동 승인(ACTIVE) 또는 관리자 확인 대기(PENDING) 상태를 결정합니다.
     */
    @Transactional
    public void registerVehicle(Long userId, VehicleRegistrationRequestDto requestDto) {

        // 1. 유저 존재 및 상태 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (user.getStatus() != Status.ACTIVE) {
            throw new CustomException(ErrorCode.USER_SUSPENDED);
        }

        // 2. 차량 번호 중복 체크 (기존 등록 여부 확인)
        Optional<Vehicle> existingVehicleOpt = vehicleRepository.findByCarNumber(requestDto.getCarNumber());

        if (existingVehicleOpt.isPresent()) {
            Vehicle existingVehicle = existingVehicleOpt.get();

            if (existingVehicle.getStatus() == VehicleStatus.ACTIVE) {
                throw new CustomException(ErrorCode.DUPLICATE_VEHICLE);
            } else if (existingVehicle.getStatus() == VehicleStatus.PENDING) {
                throw new CustomException(ErrorCode.VEHICLE_ALREADY_PENDING);
            }
            // DELETED 상태인 경우는 기존 레코드를 재사용하므로 통과
        }

        // 3. OCR 원본 데이터 유효성 확인 (보안을 위해 모든 원본 필드 검사)
        if (requestDto.getOcrRawName() == null || requestDto.getIdCardRawName() == null ||
                requestDto.getOcrRawCarNumber() == null || requestDto.getOcrRawVehicleName() == null) {
            throw new CustomException(ErrorCode.OCR_DATA_MISSING);
        }

        // 4. 보안 검증 로직 실행
        //
        // ※ 자동 승인(ACTIVE) 조건 - 아래 6가지를 모두 만족해야 합니다:
        //   ① 이름 무수정   : 사용자가 OCR 읽어온 이름을 그대로 제출 (수정 없음)
        //   ② 차량번호 무수정: 사용자가 OCR 읽어온 차량번호를 그대로 제출 (수정 없음)
        //   ③ 차종 무수정   : 사용자가 OCR 읽어온 차종을 그대로 제출 (수정 없음)
        //   ④ 생년월일 무수정: 사용자가 OCR 읽어온 생년월일을 그대로 제출 (수정 없음)
        //                     (DB 포맷 불일치 문제로 DB 비교 없이 신분증 원본과만 비교)
        //   ⑤ 유저 명의 일치: 차량등록증 OCR 이름 ≈ DB 회원 이름 (유사도 threshold% 이상)
        //   ⑥ 서류 명의 일치: 차량등록증 OCR 이름 ≈ 신분증 OCR 이름 (유사도 threshold% 이상)
        //
        // ※ 승인 대기(PENDING)로 떨어지는 경우:
        //   - 이름·차량번호·차종·생년월일 중 하나라도 OCR 원본과 다르게 수정되어 제출된 경우
        //     (OCR 오류로 인한 수정이라도 관리자가 서류를 직접 확인해야 하므로 PENDING 처리)
        //   - 차량등록증 이름과 DB 회원 이름의 유사도가 threshold 미만인 경우 (타인 명의 의심)
        //   - 차량등록증 이름과 신분증 이름의 유사도가 threshold 미만인 경우 (서류 불일치)

        // (1) 인풋 수정 여부: 이름, 차번호, 차종, 생년월일 중 하나라도 원본과 다르면 '수정됨' 판정
        boolean isNameNotModified = requestDto.getName().equals(requestDto.getOcrRawName());
        boolean isCarNumberNotModified = requestDto.getCarNumber().equals(requestDto.getOcrRawCarNumber());
        boolean isVehicleNameNotModified = requestDto.getVehicleName().equals(requestDto.getOcrRawVehicleName());
        // 생년월일: DB 포맷 불일치 문제로 DB 비교는 하지 않고, 신분증 OCR 원본과만 비교
        // 프론트에서 idCardRawBirth를 정규화(6자리)로 저장하므로 birth와 동일 기준으로 비교 가능
        boolean isBirthNotModified = requestDto.getBirth().equals(requestDto.getIdCardRawBirth());

        boolean isNotModified = isNameNotModified && isCarNumberNotModified && isVehicleNameNotModified && isBirthNotModified;

        // (2) 명의 일치 유사도 계산 (등록증 vs 로그인 유저)
        double userSimilarity = jaroWinklerMatcher.match(requestDto.getOcrRawName(), user.getName());

        // (3) 서류 간 일치 유사도 계산 (등록증 vs 신분증)
        double docSimilarity = jaroWinklerMatcher.match(requestDto.getOcrRawName(), requestDto.getIdCardRawName());

        // 5. 자동 승인 임계치(Threshold) 조회
        int threshold = systemSettingRepository.findBySettingKey(SettingKey.VEHICLE_AUTO_APPROVAL_THRESHOLD.getKey())
                .map(s -> Integer.parseInt(s.getSettingValue()))
                .orElse(90); // 설정 미비 시 기본 90%

        // 6. 최종 상태 결정
        // → 무수정(①②③) AND 유저 유사도 통과(④) AND 서류 유사도 통과(⑤) : 자동 승인(ACTIVE)
        // → 하나라도 미달 : 관리자 수동 검토 대기(PENDING)
        VehicleStatus finalStatus = VehicleStatus.PENDING;
        if (isNotModified && (userSimilarity * 100 >= threshold) && (docSimilarity * 100 >= threshold)) {
            finalStatus = VehicleStatus.ACTIVE;
        }

        log.info("--- [차량 등록 판정] 유저: {}, 상태: {}, 무수정(이름/차번/차종/생일): {}/{}/{}/{}, 유저유사도: {}%, 서류유사도: {}% ---",
                user.getName(), finalStatus,
                isNameNotModified, isCarNumberNotModified, isVehicleNameNotModified, isBirthNotModified,
                userSimilarity * 100, docSimilarity * 100);

        // 7. Vehicle 데이터 처리 (신규 생성 또는 기존 DELETED 데이터 업데이트)
        VehicleStatus finalStatusToSave = finalStatus;
        Vehicle vehicle = existingVehicleOpt.map(v -> {
            v.updateRegistration(user, requestDto.getVehicleName(), finalStatusToSave);
            return vehicleRepository.save(v);
        }).orElseGet(() -> {
            Vehicle newVehicle = Vehicle.builder()
                    .user(user)
                    .vehicleName(requestDto.getVehicleName())
                    .carNumber(requestDto.getCarNumber())
                    .status(finalStatusToSave)
                    .build();
            return vehicleRepository.save(newVehicle);
        });

        // 8. 결재(Approval) 레코드 생성
        ApprovalStatus approvalStatus = (finalStatus == VehicleStatus.ACTIVE) ? ApprovalStatus.APPROVED : ApprovalStatus.PENDING;
        Approval approval = Approval.builder()
                .approvalType(ApprovalType.VEHICLE)
                .targetId(vehicle.getId())
                .requestUserId(user)
                .status(approvalStatus)
                .processedAt(finalStatus == VehicleStatus.ACTIVE ? LocalDateTime.now() : null)
                .build();
        approvalRepository.save(approval);

        // 9. 활동 로그 기록
        ActivityLog activityLog = ActivityLog.builder()
                .activityType(ActivityType.VEHICLE_REGISTERED)
                .user(user)
                .carNumber(vehicle.getCarNumber())
                .household(user.getHousehold())
                .message(String.format("차량 등록: %s (유사도-유저: %.1f%%, 서류: %.1f%%, 상태: %s, 무수정: %b)",
                        vehicle.getCarNumber(), userSimilarity * 100, docSimilarity * 100, finalStatus, isNotModified))
                .build();
        activityLogRepository.save(activityLog);
    }

    /**
     * [내 차량 정보 조회]
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
     * [차량 등록 신청 취소]
     * - PENDING 상태인 신청 건에 대해서만 소유권 해제 및 소프트 삭제 처리
     */
    @Transactional
    public void cancelVehicleRegistration(Long userId, VehicleCancelRequestDto requestDto) {
        Vehicle vehicle = vehicleRepository.findById(requestDto.getVehicleId())
                .orElseThrow(() -> new CustomException(ErrorCode.VEHICLE_NOT_FOUND));

        if (vehicle.getUser() == null || !vehicle.getUser().getUserId().equals(userId)) {
            throw new CustomException(ErrorCode.ACCESS_DENIED_VEHICLE);
        }

        if (vehicle.getStatus() != VehicleStatus.PENDING) {
            throw new CustomException(ErrorCode.INVALID_VEHICLE_STATUS);
        }

        Approval approval = approvalRepository.findByTargetIdAndApprovalType(vehicle.getId(), ApprovalType.VEHICLE)
                .filter(a -> a.getStatus() == ApprovalStatus.PENDING)
                .orElseThrow(() -> new CustomException(ErrorCode.APPROVAL_NOT_FOUND));

        approval.cancel();
        vehicle.softDelete();
        vehicle.assignUser(null);
    }

    /**
     * [활성화된 차량 삭제]
     * - 입차 중이거나 유효한 정기권이 있는 경우 삭제 불가
     */
    @Transactional
    public void deleteVehicle(Long userId, Long vehicleId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new CustomException(ErrorCode.VEHICLE_NOT_FOUND));

        if (vehicle.getUser() == null || !vehicle.getUser().getUserId().equals(userId)) {
            throw new CustomException(ErrorCode.ACCESS_DENIED_VEHICLE);
        }

        // 주차 베네핏 적용 여부 확인
        if (parkingLogRepository.existsActiveBenefitLogByVehicleId(vehicleId)) {
            throw new CustomException(ErrorCode.CANNOT_DELETE_VEHICLE_IN_PARKING);
        }

        // 유효한(현재 또는 미래) 정기권 여부 확인
        if (subscriptionRepository.hasActiveOrFutureSubscription(vehicleId, LocalDateTime.now())) {
            throw new CustomException(ErrorCode.CANNOT_DELETE_VEHICLE_WITH_SUBSCRIPTION);
        }

        vehicle.softDelete();
        vehicle.assignUser(null);
    }
}