package com.example.demo.domain.user.report.repository;

import com.example.demo.domain.user.report.entity.VehicleReportStat;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VehicleReportRepository extends JpaRepository<VehicleReportStat,String > {
}
