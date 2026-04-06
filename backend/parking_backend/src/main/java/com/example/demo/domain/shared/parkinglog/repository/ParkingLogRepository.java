package com.example.demo.domain.shared.parkinglog.repository;

import com.example.demo.domain.shared.parkinglog.ParkingLog;
import com.example.demo.domain.shared.parkinglog.dtos.response.VehicleSearchResponseDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ParkingLogRepository extends JpaRepository<ParkingLog,Long>, ParkingLogRepositoryCustom {
    //차량번호 4자리 입력 후 차량 조회 시 조회될 차량번호 목록
    @Query("select new com.example.demo.domain.shared.parkinglog.dtos.response.VehicleSearchResponseDto(p.parkingLogId,p.carNumberSnapshot)" +
            "from ParkingLog p where p.carNumberSnapshot like %:vehicleNumber and p.exitedAt is null")
    List<VehicleSearchResponseDto> getActiveVehicleList(@Param("vehicleNumber") String vehicleNumber);


}
