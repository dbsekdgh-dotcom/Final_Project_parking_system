package com.example.demo.domain.report.repository;

import com.example.demo.domain.report.entity.VehicleReportStat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VehicleReportRepository extends JpaRepository<VehicleReportStat,String > {
    List<VehicleReportStat> findByCarNumber(String carNumber);
}
