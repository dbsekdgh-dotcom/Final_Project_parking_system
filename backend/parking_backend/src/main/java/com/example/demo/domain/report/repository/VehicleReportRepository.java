package com.example.demo.domain.report.repository;

import com.example.demo.domain.report.entity.VehicleReportStat;
import com.example.demo.domain.vehicle.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface VehicleReportRepository extends JpaRepository<VehicleReportStat,String > {

    // 이 쿼리가 핵심입니다! DB의 값과 입력값 양쪽에서 공백을 제거하고 비교합니다.
    @Query("SELECT v FROM Vehicle v WHERE REPLACE(v.carNumber, ' ', '') = :carNumber AND v.status = 'ACTIVE'")
    Optional<Vehicle> findByCarNumberIgnoreSpaces(@Param("carNumber") String carNumber);

    List<VehicleReportStat> findByCarNumber(String carNumber);
}
