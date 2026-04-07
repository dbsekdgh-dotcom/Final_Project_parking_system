package com.example.demo.domain.kiosk.entry.service;

import com.example.demo.domain.kiosk.entry.dtos.response.CameraResponse;
import com.example.demo.domain.kiosk.entry.dtos.response.EntryCheckResponse;
import com.example.demo.domain.kiosk.entry.dtos.response.OcrResponse;
import com.example.demo.domain.kiosk.entry.repository.*;
import com.example.demo.domain.shared.camera.enums.CameraType;
import com.example.demo.domain.shared.camera.Camera;
import com.example.demo.domain.shared.parkingfeepolicy.ParkingFeePolicy;
import com.example.demo.domain.shared.parkingfeepolicy.enums.ParkingType;
import com.example.demo.domain.shared.parkinglog.ParkingLog;
import com.example.demo.domain.shared.parkinglog.enums.ParkingStatus;
import com.example.demo.domain.shared.parkinglog.enums.ParkingTypeSnapshot;
import com.example.demo.domain.shared.parkinglog.enums.PaymentStatus;
import com.example.demo.domain.shared.parkinglog.repository.ParkingLogRepository;
import com.example.demo.domain.shared.vehicle.Vehicle;
import com.example.demo.global.exception.BusinessException;
import com.example.demo.global.exception.ErrorCode;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@Service
@Transactional
@RequiredArgsConstructor
public class EntryService {
    private final AiClient aiClient;
    private final ParkingLogRepository parkinglogRepository;
    private final EntryVehicleRepository entryVehicleRepository;
    private final EntryVehicleBlacklistRepository entryVehicleBlacklistRepository;
    private final EntryCameraRepository entryCameraRepository;
    private final EntryReservationRepository entryReservationRepository;
    private final EntryParkingFeePolicyRepository entryParkingFeePolicyRepository;
    @PersistenceContext
    private EntityManager entityManager;



    public Long detectedEntry(MultipartFile file){
        OcrResponse ocr= aiClient.requestOcr(file);
        String carNumber=ocr.getPlateNumber();
        EntryCheckResponse info = entryVehicleRepository
                .findEntryCheckInfo(carNumber)
                .orElse(null);
        boolean isMemberVehicle = info !=null;
        boolean isReservation=entryReservationRepository.existsValidReservation(carNumber);
        boolean isPrivilegedVehicle = (isMemberVehicle && (info.isResident()||info.isHasActiveSubscription())) ||isReservation;
        boolean isBlacklist=entryVehicleBlacklistRepository.existsActiveBlacklist(carNumber);
        boolean allowEntry=isPrivilegedVehicle || !isBlacklist;
        ParkingType policyType= isReservation ? ParkingType.RESERVATION : ParkingType.VISIT;
        ParkingFeePolicy policy= entryParkingFeePolicyRepository.findActivePolicy(policyType).orElseThrow(()->
                new BusinessException(ErrorCode.PARKING_POLICY_NOT_FOUND));
        ParkingTypeSnapshot typeSnapshot;
        if (info != null && info.isResident()){
            typeSnapshot = ParkingTypeSnapshot.RESIDENT;
        } else if (isReservation) {
            typeSnapshot = ParkingTypeSnapshot.RESERVATION;
        } else {
            typeSnapshot = ParkingTypeSnapshot.VISIT;
        }
        Vehicle vehicle = isMemberVehicle ? entityManager.getReference(
                Vehicle.class,
                info.getVehicleId()
        ):null;
        if(allowEntry){
            ParkingLog log= ParkingLog.builder().
                    vehicle(vehicle).
                    carNumberSnapshot(carNumber).
                    isBlacklist(isBlacklist).
                    parkingTypeSnapshot(typeSnapshot).
                    paymentStatus(PaymentStatus.NONE).
                    parkingStatus(ParkingStatus.DETECTED).
                    parkingFeePolicyId(policy.getId()).
                    fee(0).
                    calculatedFee(0L).
                    totalDiscountMinutes(0).
                    totalDiscountAmount(0).
                    rawFee(0).
                    graceMinutesSnapshot(policy.getGraceMinutes()).
                    entryPlateImage(ocr.getS3path()).
                    build();
           ParkingLog saved= parkinglogRepository.save(log);
           return saved.getParkingLogId();
        }
        return null;
    }
    public List<CameraResponse> getEntryCameras() {
        return entryCameraRepository.findAllByCameraType(CameraType.ENTRY)
                .stream()
                .map(CameraResponse::new)
                .toList();
    }

    public void enterWithCamera(Long parkingLogId, Long cameraId) {
        ParkingLog log = parkinglogRepository.findById(parkingLogId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
        Camera camera = entryCameraRepository.findById(cameraId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
        log.enter(camera);
        parkinglogRepository.save(log);
    }
}
