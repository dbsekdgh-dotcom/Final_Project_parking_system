package com.example.demo.domain.report.repository;

import com.example.demo.domain.report.entity.VehicleReportStat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VehicleReportStatRepository extends JpaRepository<VehicleReportStat,String> {

    Optional<VehicleReportStat> findByCarNumber(String carNumber);
}
