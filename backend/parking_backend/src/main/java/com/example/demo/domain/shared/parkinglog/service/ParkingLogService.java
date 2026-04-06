package com.example.demo.domain.shared.parkinglog.service;

import com.example.demo.domain.shared.parkinglog.dtos.response.ParkingLogSummaryResponse;
import com.example.demo.domain.shared.parkinglog.dtos.response.VehicleSearchResponseDto;
import com.example.demo.domain.shared.parkinglog.repository.ParkingLogRepository;
import com.example.demo.global.exception.BusinessException;
import com.example.demo.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 기본을 읽기전용으로 설정 - 추후 db 저장/수정 하는 메서드에만 일반 @Transactional을 붙여주면 됨
public class ParkingLogService {
    private final ParkingLogRepository parkinglogRepository;

    //차량번호 4자리 입력 후 차량 조회 시 조회될 차량번호 목록
    public List<VehicleSearchResponseDto> getActiveVehicleList(String vehicleNumber){
        List<VehicleSearchResponseDto> list=parkinglogRepository.getActiveVehicleList(vehicleNumber.trim());
        if(list==null || list.isEmpty()){
            throw new BusinessException(ErrorCode.VEHICLE_NOT_ENTERED);
        }
        return list;
    }

    //관리자 입출차 기록 페이지 상단 요약정보 4가지 정보 조회(현재주차,금일출차완료,미납,금일로그)
    public ParkingLogSummaryResponse getMainSummary(){
        return parkinglogRepository.getParkingSummary();
    }
}
