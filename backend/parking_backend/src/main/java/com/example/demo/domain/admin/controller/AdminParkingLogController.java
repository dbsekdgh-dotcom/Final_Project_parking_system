package com.example.demo.domain.admin.controller;

import com.example.demo.domain.shared.parkinglog.dtos.response.ParkingLogDetailResponse;
import com.example.demo.domain.shared.parkinglog.dtos.response.ParkingLogListResponse;
import com.example.demo.domain.shared.parkinglog.dtos.response.ParkingLogSummaryResponse;
import com.example.demo.domain.shared.parkinglog.service.ParkingLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminParkingLogController {
    private final ParkingLogService parkingLogService;

    //관리자 입출차기록 상단 요약정보 조회
    @GetMapping("/parking/summary")
    public ResponseEntity<ParkingLogSummaryResponse> getParkingSummary(){
        ParkingLogSummaryResponse summary = parkingLogService.getMainSummary();

        return ResponseEntity.ok(summary);
    }

    //관리자 입출차기록 하단 내역 테이블 조회 + 페이징
    @GetMapping("/parking/logs")
    public ResponseEntity<Page<ParkingLogListResponse>> getParkingLogList(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "ALL")String status,
            @PageableDefault(size = 5, sort = "parkingLogId", direction = Sort.Direction.DESC) Pageable pageable){
        Page<ParkingLogListResponse> response = parkingLogService.getParkingLogList(keyword,status,pageable);
        return ResponseEntity.ok(response);
    }

    //특정 입출차 기록 상세 조회
    @GetMapping("/parking/logs/{parkingLogId}")
    public ResponseEntity<ParkingLogDetailResponse> getParkingLogDetail(@PathVariable Long parkingLogId){
        ParkingLogDetailResponse response = parkingLogService.getParkingLogDetail(parkingLogId);
        return ResponseEntity.ok(response);
    }
}
