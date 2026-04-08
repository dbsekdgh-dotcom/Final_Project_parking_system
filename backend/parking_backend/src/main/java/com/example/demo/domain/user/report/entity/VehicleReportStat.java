package com.example.demo.domain.user.report.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "vehicle_report_stat")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleReportStat {

    @Id
    @Column(name = "car_number", length = 25)
    private String carNumber;

    //유효 신고 수
    @Column(name = "valid_report_count")
    private int validReportCount;

    //전체 신고 수
    @Column(name = "total_report_count")
    private int totalReportCount;

    @Column(name = "last_reported_at")
    private LocalDateTime lastReportedAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    //비즈니스 로직

    public void increaseTotal(){
        this.totalReportCount++;
        this.lastReportedAt = LocalDateTime.now();
    }

    public void increaseValid(){
        this.validReportCount++;
    }

    public static VehicleReportStat create(String carNumber){
        return VehicleReportStat.builder()
                .carNumber(carNumber)
                .validReportCount(0)
                .totalReportCount(0)
                .build();
    }
}
