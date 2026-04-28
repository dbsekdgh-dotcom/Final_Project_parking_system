package com.example.demo.api.user.report;

import com.example.demo.domain.report.entity.VehicleReportStat;
import com.example.demo.domain.report.service.VehicleReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "12. 차량 신고 통계 (Vehicle Report Stats)", description = "특정 차량의 누적 신고 통계 조회 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user/stats")
public class VehicleReportController {

    private final VehicleReportService vehicleReportService;

    @Operation(summary = "차량 신고 통계 조회", description = "차량번호로 해당 차량의 누적 신고 횟수, 유형별 통계 등을 반환합니다.")
    //특정 차량의 누적 신고 통계 가져오기
    @GetMapping("/vehicle/{carNumber}")
    public ResponseEntity<VehicleReportStat> getVehicleStat(@PathVariable String carNumber){
        VehicleReportStat stat = vehicleReportService.getStatCarNumber(carNumber);
        return ResponseEntity.ok(stat);
    }

}
