package com.example.demo.domain.shared.parkinglog.repository;

import com.example.demo.domain.shared.parkinglog.ParkingLog;
import com.example.demo.domain.shared.parkinglog.dtos.response.VehicleSearchResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ParkingLogRepository extends JpaRepository<ParkingLog,Long>, ParkingLogRepositoryCustom {
    //차량번호 4자리 입력 후 차량 조회 시 조회될 차량번호 목록
    @Query("select new com.example.demo.domain.shared.parkinglog.dtos.response.VehicleSearchResponseDto(p.parkingLogId,p.carNumberSnapshot)" +
            "from ParkingLog p where p.carNumberSnapshot like %:vehicleNumber and p.exitedAt is null")
    List<VehicleSearchResponseDto> getActiveVehicleList(@Param("vehicleNumber") String vehicleNumber);

    //차량번호 스냅샷에 키워드가 포함된 데이터를 페이징하여 조회
    Page<ParkingLog> findByCarNumberSnapshotContaining(String carNumber, Pageable pageable);
}
