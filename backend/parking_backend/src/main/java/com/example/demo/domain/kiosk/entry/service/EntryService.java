package com.example.demo.domain.kiosk.entry.service;

import com.example.demo.domain.kiosk.entry.dtos.response.EntryCheckResponse;
import com.example.demo.domain.kiosk.entry.dtos.response.OcrResponse;
import com.example.demo.domain.kiosk.entry.repository.*;
import com.example.demo.domain.shared.parkingfeepolicy.ParkingFeePolicy;
import com.example.demo.domain.shared.parkingfeepolicy.enums.ParkingType;
import com.example.demo.domain.shared.parkinglog.ParkingLog;
import com.example.demo.domain.shared.parkinglog.enums.ParkingStatus;
import com.example.demo.domain.shared.parkinglog.enums.ParkingTypeSnapshot;
import com.example.demo.domain.shared.parkinglog.enums.PaymentStatus;
import com.example.demo.domain.shared.parkinglog.repository.ParkinglogRepository;
import com.example.demo.domain.shared.vehicle.Vehicle;
import com.example.demo.global.exception.BusinessException;
import com.example.demo.global.exception.ErrorCode;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;


@Service
@Transactional
@RequiredArgsConstructor
public class EntryService {
    private final AiClient aiClient;
    private final ParkinglogRepository parkinglogRepository;
    private final EntryVehicleRepository entryVehicleRepository;
    private final EntryVehicleBlacklistRepository entryVehicleBlacklistRepository;
    private final EntryCameraRepository entryCameraRepository;
    private final EntryReservationRepository entryReservationRepository;
    private final EntryParkingFeePolicyRepository entryParkingFeePolicyRepository;
    @PersistenceContext
    private EntityManager entityManager;



    public void detectedEntry(MultipartFile file){
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
        if (info.isResident()){
            typeSnapshot = ParkingTypeSnapshot.RESIDENT;
        } else if (isReservation) {
            typeSnapshot = ParkingTypeSnapshot.RESERVATION;
        }else {
            typeSnapshot = ParkingTypeSnapshot.VISIT;
        }
        Vehicle vehicle = isMemberVehicle ? entityManager.getReference(
                Vehicle.class,
                info.getVehicleId()
        ):null;
        if(allowEntry){
            ParkingLog log= ParkingLog.builder().
                    vehicle(vehicle).
                    carNumberSnapshot(info.getCarNumber()).
                    isBlacklist(isBlacklist).
                    parkingTypeSnapshot(typeSnapshot).
                    paymentStatus(PaymentStatus.NONE).
                    parkingStatus(ParkingStatus.DETECTED).
                    parkingFeePolicyId(policy.getId()).
                    graceMinutesSnapshot(policy.getGraceMinutes()).
                    entryPlateImage(ocr.getS3path()).
                    build();
            parkinglogRepository.save(log);
        }

    }
}
