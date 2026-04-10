package com.example.demo.domain.kiosk.entry.service;

import com.example.demo.domain.kiosk.entry.dtos.response.CameraResponse;
import com.example.demo.domain.kiosk.entry.dtos.response.EntryCheckResponse;
import com.example.demo.domain.kiosk.entry.dtos.response.ParkingLogTypeResponse;
import com.example.demo.domain.kiosk.entry.dtos.response.ParkingSpaceResponse;
import com.example.demo.domain.kiosk.entry.repository.*;
import com.example.demo.domain.shared.activityLog.ActivityLog;
import com.example.demo.domain.shared.activityLog.repository.ActivityLogRepository;
import com.example.demo.domain.shared.camera.enums.CameraType;
import com.example.demo.domain.shared.household.Household;
import com.example.demo.domain.shared.parkingfeepolicy.ParkingFeePolicy;
import com.example.demo.domain.shared.parkingfeepolicy.enums.ParkingType;
import com.example.demo.domain.shared.parkinglog.ParkingLog;
import com.example.demo.domain.shared.parkinglog.enums.ParkingStatus;
import com.example.demo.domain.shared.parkinglog.enums.ParkingTypeSnapshot;
import com.example.demo.domain.shared.parkinglog.enums.PaymentStatus;
import com.example.demo.domain.shared.parkinglog.repository.ParkingLogRepository;
import com.example.demo.domain.shared.parkingspace.ParkingSpace;
import com.example.demo.domain.shared.parkingspace.enums.Floor;
import com.example.demo.domain.shared.parkingspace.enums.SpaceStatus;
import com.example.demo.domain.shared.vehicle.Vehicle;
import com.example.demo.global.exception.BusinessException;
import com.example.demo.global.exception.ErrorCode;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;


@Service
@Transactional
@RequiredArgsConstructor
public class EntryService {
    private final ParkingLogRepository parkinglogRepository;
    private final EntryVehicleRepository entryVehicleRepository;
    private final EntryVehicleBlacklistRepository entryVehicleBlacklistRepository;
    private final EntryCameraRepository entryCameraRepository;
    private final EntryReservationRepository entryReservationRepository;
    private final EntryParkingFeePolicyRepository entryParkingFeePolicyRepository;
    private final EntryParkingSpaceRepository entryParkingSpaceRepository;
    private final EntrySubscriptionRepository entrySubscriptionRepository;
    private final EntrySystemSettingRepository entrySystemSettingRepository;
    private final ActivityLogRepository activityLogRepository;
    @PersistenceContext
    private EntityManager entityManager;



    public Long detectedEntry(String carNumber,String s3path, Long cameraId){
        EntryCheckResponse info = entryVehicleRepository
                .findEntryCheckInfo(carNumber)
                .orElse(null);
        boolean isMemberVehicle = info != null;
        boolean isReservation = entryReservationRepository.existsValidReservation(carNumber);
        boolean isBlacklist = entryVehicleBlacklistRepository.existsActiveBlacklist(carNumber);

        // 블랙리스트 예외 대상: RESIDENT와 유효한 SUBSCRIPTION은 블랙리스트와 무관하게 무조건 입차 허용
        // RESERVATION, USER, VISIT은 블랙리스트 적용 대상
        boolean isBlacklistExempt = isMemberVehicle && (info.isResident() || info.isHasActiveSubscription());
        boolean allowEntry = isBlacklistExempt || !isBlacklist;
        ParkingType policyType= isReservation ? ParkingType.RESERVATION : ParkingType.VISIT;
        ParkingFeePolicy policy= entryParkingFeePolicyRepository.findActivePolicy(policyType).orElseThrow(()->
                new BusinessException(ErrorCode.PARKING_POLICY_NOT_FOUND));
        // 타입 우선순위: RESIDENT > RESERVATION > SUBSCRIPTION > USER > VISIT
        // SUBSCRIPTION은 기존 코드에서 VISIT으로 잘못 분류되던 버그 수정
        // USER는 앱 가입 회원이지만 정기권/입주민/예약 모두 아닌 경우
        ParkingTypeSnapshot typeSnapshot;
        if (info != null && info.isResident()) {
            typeSnapshot = ParkingTypeSnapshot.RESIDENT;
        } else if (isReservation) {
            typeSnapshot = ParkingTypeSnapshot.RESERVATION;
        } else if (isMemberVehicle && info.isHasActiveSubscription()) {
            typeSnapshot = ParkingTypeSnapshot.SUBSCRIPTION;
        } else if (isMemberVehicle) {
            typeSnapshot = ParkingTypeSnapshot.USER;
        } else {
            typeSnapshot = ParkingTypeSnapshot.VISIT;
        }
        Vehicle vehicle = isMemberVehicle ? entityManager.getReference(
                Vehicle.class,
                info.getVehicleId()
        ):null;

        if(!allowEntry){
            // 블랙리스트 차량도 기록을 남기고 거부
            ParkingLog rejected = ParkingLog.builder()
                    .vehicle(vehicle)
                    .carNumberSnapshot(carNumber)
                    .isBlacklist(true)
                    .parkingTypeSnapshot(typeSnapshot)
                    .paymentStatus(PaymentStatus.NONE)
                    .parkingStatus(ParkingStatus.BLACKLIST_REJECTED)
                    .parkingFeePolicyId(policy.getId())
                    .entryCameraId(cameraId)
                    .fee(0)
                    .calculatedFee(0L)
                    .totalDiscountMinutes(0)
                    .totalDiscountAmount(0)
                    .rawFee(0)
                    .graceMinutesSnapshot(policy.getGraceMinutes())
                    .entryPlateImage(s3path)
                    .build();
            parkinglogRepository.save(rejected);
            throw new BusinessException(ErrorCode.BLACKLIST_VEHICLE);
        }
        //실제 입차한 차량
        ParkingLog log= ParkingLog.builder().
                vehicle(vehicle).
                carNumberSnapshot(carNumber).
                isBlacklist(isBlacklist).
                parkingTypeSnapshot(typeSnapshot).
                paymentStatus(PaymentStatus.NONE).
                parkingStatus(ParkingStatus.DETECTED).
                parkingFeePolicyId(policy.getId()).
                entryCameraId(cameraId).
                fee(0).
                calculatedFee(0L).
                totalDiscountMinutes(0).
                totalDiscountAmount(0).
                rawFee(0).
                graceMinutesSnapshot(policy.getGraceMinutes()).
                entryPlateImage(s3path).
                build();
        ParkingLog saved= parkinglogRepository.save(log);
        return saved.getParkingLogId();
    }

    public List<CameraResponse> getEntryCameras() {
        return entryCameraRepository.findAllByCameraType(CameraType.ENTRY)
                .stream()
                .map(CameraResponse::new)
                .toList();
    }

    public List<CameraResponse> getExitCameras() {
        return entryCameraRepository.findAllByCameraType(CameraType.EXIT)
                .stream()
                .map(CameraResponse::new)
                .toList();
    }

    //DETECTED -> ENTERED 확정 시점
    public void enterWithCamera(Long parkingLogId, Long spaceId) {
        //ENTRY_LOCK 행에 FOR UPDATE 이 시점부터 다른 입차 트랜잭션 대기
        entrySystemSettingRepository.findByIdWithLock("ENTRY_LOCK")
                .orElseThrow(()-> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
        //실제 잔여 자리 조회
        long available = entryParkingSpaceRepository.countAvailableSpace();
        if (available<=0){
            throw new BusinessException(ErrorCode.PARKING_FULL);
        }

        ParkingLog log = parkinglogRepository.findById(parkingLogId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
        ParkingSpace space = entryParkingSpaceRepository.findByIdWithLock(spaceId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
        if (space.getStatus() != SpaceStatus.AVAILABLE) {
            throw new BusinessException(ErrorCode.SPACE_NOT_AVAILABLE);
        }
        log.setParkingSpace(space);
        space.setStatus(SpaceStatus.OCCUPIED);

        LocalDateTime freeExitUntil = resolveFreeExitUntil(log);
        log.enter(freeExitUntil);
        parkinglogRepository.save(log);
        Household household = (log.getVehicle()!=null && log.getVehicle().getUser()!=null)
                ? log.getVehicle().getUser().getHousehold():null;
        activityLogRepository.save(ActivityLog.ofEntry(log,household));
    }

    // 타입별 최초 회차 시간 계산
    // RESIDENT        : null → 무제한 (요금 발생 없음)
    // SUBSCRIPTION    : 정기권 만료일 → 만료일까지 무료
    // VISIT/USER/RESERVATION : 입차 시간 + graceMinutesSnapshot → 이후 요금 발생
    private LocalDateTime resolveFreeExitUntil(ParkingLog log) {
        LocalDateTime now = LocalDateTime.now();
        return switch (log.getParkingTypeSnapshot()) {
            case RESIDENT -> LocalDateTime.of(3000, 1, 1, 0, 0);
            case SUBSCRIPTION -> {
                Long vehicleId = log.getVehicle().getId();
                yield entrySubscriptionRepository.findActiveSubscriptionEndDate(vehicleId)
                        .orElse(now.plusMinutes(log.getGraceMinutesSnapshot()));
            }
            // VISIT, USER, RESERVATION 모두 grace_minutes 만큼 무료
            default -> now.plusMinutes(log.getGraceMinutesSnapshot());
        };
    }

    public ParkingLogTypeResponse getParkingType(Long parkingLogId) {
        ParkingLog log = parkinglogRepository.findById(parkingLogId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
        return new ParkingLogTypeResponse(log.getParkingTypeSnapshot().name());
    }

    public List<ParkingSpaceResponse> getSpacesByFloor(String floor) {
        Floor floorEnum = Floor.valueOf(floor);
        return entryParkingSpaceRepository.findByFloorAndStatusNot(floorEnum, SpaceStatus.BLOCKED)
                .stream()
                .map(ParkingSpaceResponse::new)
                .toList();
    }

    public void cancelEntry(Long parkingLogId) {
        ParkingLog log = parkinglogRepository.findById(parkingLogId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
        log.cancel();
    }

    // 차번호로 현재 ENTERED 상태인 레코드 조회 (입차/출차 버튼 분기용)
    @Transactional(readOnly = true)
    public Map<String, Object> checkEntered(String carNumber) {
        return parkinglogRepository
                .findFirstByCarNumberSnapshotAndParkingStatus(carNumber, ParkingStatus.ENTERED)
                .map(log -> Map.<String, Object>of(
                        "isEntered", true,
                        "parkingLogId", log.getParkingLogId()))
                .orElse(Map.of("isEntered", false));
    }

}
