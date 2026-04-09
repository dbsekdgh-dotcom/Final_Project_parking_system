package com.example.demo.domain.kiosk.exit.service;

import com.example.demo.domain.kiosk.payment.dtos.response.VehiclePaymentResponseDto;
import com.example.demo.domain.kiosk.payment.facade.PaymentFacade;
import com.example.demo.domain.shared.parkinglog.ParkingLog;
import com.example.demo.domain.shared.parkinglog.enums.ParkingStatus;
import com.example.demo.domain.shared.parkinglog.repository.ParkingLogRepository;
import com.example.demo.domain.shared.parkingspace.ParkingSpace;
import com.example.demo.domain.shared.parkingspace.enums.SpaceStatus;
import com.example.demo.global.exception.BusinessException;
import com.example.demo.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ExitService {
    private final ParkingLogRepository parkingLogRepository;
    private final PaymentFacade paymentFacade;

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

        //요금 계산 및 차량 검증
        return paymentFacade.paymentProcess(parkingLogId);

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
        parkingLog.setParkingStatus(ParkingStatus.EXITED);
        parkingLog.setExitedAt(LocalDateTime.now());
        parkingLogRepository.save(parkingLog);
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
