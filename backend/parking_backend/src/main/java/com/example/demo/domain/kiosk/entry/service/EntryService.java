package com.example.demo.domain.kiosk.entry.service;

import com.example.demo.domain.kiosk.entry.dtos.response.CameraResponse;
import com.example.demo.domain.kiosk.entry.dtos.response.EntryCheckResponse;
import com.example.demo.domain.kiosk.entry.dtos.response.ParkingLogTypeResponse;
import com.example.demo.domain.kiosk.entry.dtos.response.ParkingSpaceResponse;
import com.example.demo.domain.kiosk.entry.repository.EntryVehicleRepository;
import com.example.demo.domain.kiosk.exit.service.FreeExitRedisService;
import com.example.demo.domain.shared.activityLog.ActivityLog;
import com.example.demo.domain.shared.activityLog.repository.ActivityLogRepository;
import com.example.demo.domain.shared.camera.enums.CameraType;
import com.example.demo.domain.shared.camera.repository.CameraRepository;
import com.example.demo.domain.shared.household.Household;
import com.example.demo.domain.shared.parkingfeepolicy.ParkingFeePolicy;
import com.example.demo.domain.shared.parkingfeepolicy.enums.ParkingType;
import com.example.demo.domain.shared.parkingfeepolicy.repository.ParkingFeePolicyRepository;
import com.example.demo.domain.shared.parkinglog.ParkingLog;
import com.example.demo.domain.shared.parkinglog.enums.ParkingStatus;
import com.example.demo.domain.shared.parkinglog.enums.ParkingTypeSnapshot;
import com.example.demo.domain.shared.parkinglog.enums.PaymentStatus;
import com.example.demo.domain.shared.parkinglog.repository.ParkingLogRepository;
import com.example.demo.domain.shared.parkingspace.ParkingSpace;
import com.example.demo.domain.shared.parkingspace.enums.Floor;
import com.example.demo.domain.shared.parkingspace.enums.SpaceStatus;
import com.example.demo.domain.shared.parkingspace.repository.ParkingSpaceRepository;
import com.example.demo.domain.shared.reservation.repository.ReservationRepository;
import com.example.demo.domain.shared.subscription.repository.SubscriptionRepository;
import com.example.demo.domain.shared.systemSetting.repository.SystemSettingRepository;
import com.example.demo.domain.shared.vehicle.Vehicle;
import com.example.demo.domain.shared.vehicleblacklist.repository.VehicleBlacklistRepository;
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
    private final VehicleBlacklistRepository vehicleBlacklistRepository;
    private final CameraRepository cameraRepository;
    private final ReservationRepository reservationRepository;
    private final ParkingFeePolicyRepository parkingFeePolicyRepository;
    private final ParkingSpaceRepository parkingSpaceRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final SystemSettingRepository systemSettingRepository;
    private final ActivityLogRepository activityLogRepository;
    private final FreeExitRedisService freeExitRedisService;
    @PersistenceContext
    private EntityManager entityManager;



    public Long detectedEntry(String carNumber, String s3path, Long cameraId) {
        EntryCheckResponse info = entryVehicleRepository
                .findEntryCheckInfo(carNumber)
                .orElse(null);
        boolean isMemberVehicle = info != null;
        boolean isReservation = reservationRepository.existsValidReservation(carNumber);
        boolean isBlacklist = vehicleBlacklistRepository.isCurrentlyBlacklisted(carNumber, LocalDateTime.now());

        boolean isBlacklistExempt = isMemberVehicle && (info.isResident() || info.isHasActiveSubscription());
        boolean allowEntry = isBlacklistExempt || !isBlacklist;
        ParkingType policyType = isReservation ? ParkingType.RESERVATION : ParkingType.VISIT;
        ParkingFeePolicy policy = parkingFeePolicyRepository.findActivePolicy(policyType)
                .orElseThrow(() -> new BusinessException(ErrorCode.PARKING_POLICY_NOT_FOUND));

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
        ) : null;

        if (!allowEntry) {
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
        ParkingLog log = ParkingLog.builder()
                .vehicle(vehicle)
                .carNumberSnapshot(carNumber)
                .isBlacklist(isBlacklist)
                .parkingTypeSnapshot(typeSnapshot)
                .paymentStatus(PaymentStatus.NONE)
                .parkingStatus(ParkingStatus.DETECTED)
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
        ParkingLog saved = parkinglogRepository.save(log);
        return saved.getParkingLogId();
    }

    public List<CameraResponse> getEntryCameras() {
        return cameraRepository.findAllByCameraType(CameraType.ENTRY)
                .stream()
                .map(CameraResponse::new)
                .toList();
    }

    public List<CameraResponse> getExitCameras() {
        return cameraRepository.findAllByCameraType(CameraType.EXIT)
                .stream()
                .map(CameraResponse::new)
                .toList();
    }

    public void enterWithCamera(Long parkingLogId, Long spaceId) {
        systemSettingRepository.findByIdWithLock("ENTRY_LOCK")
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
        long available = parkingSpaceRepository.countAvailableSpace();
        if (available <= 0) {
            throw new BusinessException(ErrorCode.PARKING_FULL);
        }

        ParkingLog log = parkinglogRepository.findById(parkingLogId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
        ParkingSpace space = parkingSpaceRepository.findByIdWithLock(spaceId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
        if (space.getStatus() != SpaceStatus.AVAILABLE) {
            throw new BusinessException(ErrorCode.SPACE_NOT_AVAILABLE);
        }
        log.setParkingSpace(space);
        space.setStatus(SpaceStatus.OCCUPIED);

        LocalDateTime freeExitUntil = resolveFreeExitUntil(log);
        log.enter(freeExitUntil);
        parkinglogRepository.save(log);
        freeExitRedisService.register(log.getParkingLogId(), freeExitUntil);
        if (log.getParkingTypeSnapshot() == ParkingTypeSnapshot.RESERVATION) {
            reservationRepository.updateStatusToEntered(log.getCarNumberSnapshot());
        }
        Household household = (log.getVehicle() != null && log.getVehicle().getUser() != null)
                ? log.getVehicle().getUser().getHousehold() : null;
        activityLogRepository.save(ActivityLog.ofEntry(log, household));
    }

    private LocalDateTime resolveFreeExitUntil(ParkingLog log) {
        LocalDateTime now = LocalDateTime.now();
        return switch (log.getParkingTypeSnapshot()) {
            case RESIDENT -> LocalDateTime.of(3000, 1, 1, 0, 0);
            case SUBSCRIPTION -> {
                Long vehicleId = log.getVehicle().getId();
                yield subscriptionRepository.findActiveSubscriptionEndDate(vehicleId)
                        .orElse(now.plusMinutes(log.getGraceMinutesSnapshot()));
            }
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
        return parkingSpaceRepository.findByFloorAndStatusNot(floorEnum, SpaceStatus.BLOCKED)
                .stream()
                .map(ParkingSpaceResponse::new)
                .toList();
    }

    public void cancelEntry(Long parkingLogId) {
        ParkingLog log = parkinglogRepository.findById(parkingLogId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
        log.cancel();
    }

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
