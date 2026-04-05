package com.example.demo.domain.shared.parkinglog.service;

import com.example.demo.domain.shared.parkinglog.dtos.response.VehicleSearchResponseDto;
import com.example.demo.domain.shared.parkinglog.repository.ParkinglogRepository;
import com.example.demo.global.exception.BusinessException;
import com.example.demo.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ParkinglogService {
    private final ParkinglogRepository parkinglogRepository;

    //차량번호 4자리 입력 후 차량 조회 시 조회될 차량번호 목록
    public List<VehicleSearchResponseDto> getActiveVehicleList(String vehicleNumber){
        List<VehicleSearchResponseDto> list=parkinglogRepository.getActiveVehicleList(vehicleNumber.trim());
        if(list==null || list.isEmpty()){
            throw new BusinessException(ErrorCode.VEHICLE_NOT_ENTERED);
        }
        return list;
    }
}
