package com.example.demo.domain.kiosk.prepay.service;

import com.example.demo.domain.kiosk.prepay.dtos.request.VehicleExitRequest;
import com.example.demo.domain.shared.parkinglog.ParkingLog;
import com.example.demo.domain.shared.parkinglog.enums.ParkingStatus;
import com.example.demo.domain.shared.parkinglog.enums.ParkingTypeSnapshot;
import com.example.demo.domain.shared.parkinglog.repository.ParkinglogRepository;
import com.example.demo.domain.shared.subscription.repository.SubscriptionRepository;
import com.example.demo.domain.shared.vehicle.repository.VehicleRepository;
import com.example.demo.global.exception.BusinessException;
import com.example.demo.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PrepayService {
    private final ParkinglogRepository parkinglogRepository;
    private final VehicleRepository vehicleRepository;
    private final SubscriptionRepository subscriptionRepository;


    //EXIT_REQUESTED 시 요금 계산
    public void calculateParkingFee(VehicleExitRequest vehicleExitRequest){
        int exitTime;
        int rowFee;
        int totalDiscountMinutes;
        int totalDiscountAmount;
        Long calculatedFee;
        boolean feeFree=false;
        boolean isBlackList=false;

        ParkingLog parkingLog=(ParkingLog) parkinglogRepository.findById(vehicleExitRequest.getParkingLogId()).orElseThrow(()->{
            throw new BusinessException(ErrorCode.ENTITY_NOT_FOUND);
        });
        //이미 출차된 상태일 때
        if(vehicleExitRequest.getParkingStatus()== ParkingStatus.EXITED){
            throw new BusinessException(ErrorCode.ALREADY_EXITED);
        }
        //parkingTypeSnapshot 꺼내오기
        ParkingTypeSnapshot parkingTypeSnapshot=parkingLog.getParkingTypeSnapshot();

        //입주민일 때
        if(parkingTypeSnapshot.name().equals(ParkingTypeSnapshot.RESIDENT)){
            feeFree=true;
        }

        //정기권일 때
        Long vehicleId=vehicleRepository.getVehicleIdByCarNumber(parkingLog.getCarNumberSnapshot()).orElse(null);
        if(vehicleId!=null){
            Long subscriptionId=subscriptionRepository.getSubscriptionIdByVehicleId(vehicleId).orElse(null);
            if(subscriptionId!=null){
                feeFree=true;
            }
        }

        //상기 외 블랙리스트일 때
        if(!feeFree){
            isBlackList=parkingLog.getIsBlacklist();
            if(isBlackList){
                throw new BusinessException(ErrorCode.BLACKLIST_VEHICLE);
            }
        }

        //외부인/방문예약 구분


        //raw_fee계산



        //할인권 조회 할인요금 계산

        //최종 결제 금액 계산

    }

    //request prepay시 parking_log 테이블 업데이트
    public void updatePrepay(VehicleExitRequest vehicleExitRequest){

    }


}
