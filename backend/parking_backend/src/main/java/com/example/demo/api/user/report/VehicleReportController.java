package com.example.demo.api.user.report;

import com.example.demo.domain.report.entity.VehicleReportStat;
import com.example.demo.domain.report.service.VehicleReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/stats")
public class VehicleReportController {

    private final VehicleReportService vehicleReportService;

    //특정 차량의 누적 신고 통계 가져오기
    @GetMapping("/vehicle/{carNumber}")
    public ResponseEntity<VehicleReportStat> getVehicleStat(@PathVariable String carNumber){
        VehicleReportStat stat = vehicleReportService.getStatCarNumber(carNumber);
        return ResponseEntity.ok(stat);
    }
}
