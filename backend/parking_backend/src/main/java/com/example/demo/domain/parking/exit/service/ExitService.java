package com.example.demo.domain.parking.exit.service;

import com.example.demo.domain.payment.dtos.response.VehiclePaymentResponseDto;
import com.example.demo.domain.payment.service.facade.PaymentFacade;
import com.example.demo.domain.system.activitylog.ActivityLog;
import com.example.demo.domain.system.activitylog.repository.ActivityLogRepository;
import com.example.demo.domain.resident.household.Household;
import com.example.demo.domain.parking.log.ParkingLog;
import com.example.demo.domain.parking.log.enums.ParkingStatus;
import com.example.demo.domain.parking.log.enums.ParkingTypeSnapshot;
import com.example.demo.domain.parking.log.repository.ParkingLogRepository;
import com.example.demo.domain.parking.space.ParkingSpace;
import com.example.demo.domain.parking.space.enums.SpaceStatus;
import com.example.demo.domain.reservation.enums.Status;
import com.example.demo.domain.reservation.repository.ReservationRepository;
import com.example.demo.domain.vehicle.Vehicle;
import com.example.demo.domain.payment.point.entity.UserPoint;
import com.example.demo.domain.payment.point.repository.UserPointRepository;
import com.example.demo.global.exception.BusinessException;
import com.example.demo.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class ExitService {
    private final ParkingLogRepository parkingLogRepository;
    private final PaymentFacade paymentFacade;
    private final ActivityLogRepository activityLogRepository;
    private final UserPointRepository userPointRepository;
    private final FreeExitExpirationService freeExitExpirationService;
    private final FreeExitRedisService freeExitRedisService;
    private final ReservationRepository reservationRepository;

    //출차 대기
    public VehiclePaymentResponseDto requestExit(Long parkingLogId,Long exitCameraId,String imagePath){
        // 예외 상황 추가
        ParkingLog parkingLog = parkingLogRepository.findById(parkingLogId)
                .orElseThrow(()-> new BusinessException(ErrorCode.VEHICLE_NOT_ENTERED));
        // status가 Finished 형태 status면 예외처리
        if (parkingLog.getParkingStatus().isFinished()){
            throw new BusinessException(ErrorCode.ALREADY_EXITED);
        }
        // Blacklist 한번더 확인
        if (Boolean.TRUE.equals(parkingLog.getIsBlacklist())){
            throw new BusinessException(ErrorCode.BLACKLIST_VEHICLE);
        }
        // 상태 변화 및 저장 ENTERED -> EXIT_REQUESTED
        parkingLog.exitRequested(exitCameraId,imagePath);
        parkingLogRepository.save(parkingLog);

        freeExitExpirationService.syncFreeExitStatus(parkingLog);
        //요금 계산 및 차량 검증
        return paymentFacade.paymentProcess(parkingLogId);

    }
    public int getUserPoint(Long parkingLogId){
        return parkingLogRepository.findById(parkingLogId)
                .map(ParkingLog::getVehicle)
                .map(Vehicle::getUser)
                .flatMap(user->userPointRepository.findByUserUserId(user.getUserId()))
                .map(UserPoint::getCurrentPoint)
                .orElse(0);
    }
    // 출차 확정
    public void confirmExit(Long parkingLogId){
        //예외 추가
        ParkingLog parkingLog = parkingLogRepository.findById(parkingLogId)
                .orElseThrow(() -> new BusinessException(ErrorCode.VEHICLE_NOT_ENTERED));
        //변할 수 있는 상태 값인지 확인 ex) DETECTED -> EXITED = X
        if (!parkingLog.getParkingStatus().canTransitTo(ParkingStatus.EXITED)){
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }
        // 주차 자리 상태값 변화
        ParkingSpace parkingSpace=parkingLog.getParkingSpace();
        if (parkingSpace!=null){
            parkingSpace.setStatus(SpaceStatus.AVAILABLE);
        }
        // 방문 예약 차량 값변경
        if (parkingLog.getParkingTypeSnapshot() == ParkingTypeSnapshot.RESERVATION){
            reservationRepository.updateStatusToCompleted(parkingLog.getCarNumberSnapshot(), Status.COMPLETED,Status.ENTERED);
        }
        parkingLog.setParkingStatus(ParkingStatus.EXITED);
        parkingLog.setExitedAt(LocalDateTime.now());
        parkingLogRepository.save(parkingLog);
        freeExitRedisService.delete(parkingLogId);
        Household household=(parkingLog.getVehicle()!=null&&parkingLog.getVehicle().getUser()!=null)
                ? parkingLog.getVehicle().getUser().getHousehold() : null;
        activityLogRepository.save(ActivityLog.ofExit(parkingLog,household));
    }
    // 출차 중 회차
    public void cancelExit(Long parkingLogId){
        ParkingLog parkingLog = parkingLogRepository.findById(parkingLogId)
                .orElseThrow(()->new BusinessException(ErrorCode.VEHICLE_NOT_ENTERED));
        // 상태 변화가능 여부 확인
        if (!parkingLog.getParkingStatus().canTransitTo(ParkingStatus.ENTERED)){
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }

        parkingLog.setParkingStatus(ParkingStatus.ENTERED);
        parkingLog.setExitCameraId(null);
        parkingLog.setExitPlateImage(null);
        parkingLog.setExitTime(null);
        parkingLogRepository.save(parkingLog);
    }
}
