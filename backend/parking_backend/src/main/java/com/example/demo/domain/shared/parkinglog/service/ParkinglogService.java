package com.example.demo.domain.shared.parkinglog.service;

import com.example.demo.domain.shared.parkinglog.dtos.response.VehicleSearchResponseDto;
import com.example.demo.domain.shared.parkinglog.repository.ParkinglogRepository;
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
        return parkinglogRepository.getActiveVehicleList(vehicleNumber.trim());
    }
}
