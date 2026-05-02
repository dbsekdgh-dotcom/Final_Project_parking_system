package com.example.demo.domain.report.service;

import com.example.demo.domain.notification.service.NotificationService;
import com.example.demo.domain.report.entity.VehicleReportStat;
import com.example.demo.domain.report.repository.VehicleReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VehicleReportService {
    private final VehicleReportRepository vehicleReportRepository;

    //1. 차량 번호로 통계 조회
    @Transactional(readOnly = true)
    public VehicleReportStat getStatCarNumber(String carNumber){
        return vehicleReportRepository.findById(carNumber)
                .orElseGet(()->VehicleReportStat.create(carNumber));
        //데이터가 없으면 0으로 초기화된 개 객제 반환(null 방지)
    }

    //2. 신고 발생 시 통계 업데이트 (나중에 ReportService에서 호출)
    @Transactional
    public void updateStat(String carNumber, boolean isValid){
        VehicleReportStat stat = vehicleReportRepository.findById(carNumber)
                .orElseGet(()->VehicleReportStat.create(carNumber));

        stat.increaseTotal(); //무조건 총 횟수 증가
        if(isValid){
            stat.increaseValid(); // 유효한 신고일 떄만 증가
        }
        vehicleReportRepository.save(stat);
    }
}
