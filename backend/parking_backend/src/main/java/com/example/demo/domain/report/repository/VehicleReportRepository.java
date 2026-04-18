package com.example.demo.domain.report.repository;

import com.example.demo.domain.report.entity.VehicleReportStat;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VehicleReportRepository extends JpaRepository<VehicleReportStat,String > {
}
