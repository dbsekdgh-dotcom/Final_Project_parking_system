package com.example.demo.domain.admin.management.parkingSpace.controller;


import com.example.demo.domain.admin.management.parkingSpace.dtos.response.ParkingSpaceListResponse;
import com.example.demo.domain.admin.management.parkingSpace.dtos.response.ParkingSpaceSummaryResponse;
import com.example.demo.domain.admin.management.parkingSpace.service.AdminParkingSpaceService;
import com.example.demo.domain.shared.parkingspace.enums.Floor;
import com.example.demo.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Log4j2
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/parking-space")
public class AdminParkingSpaceController {
    private final AdminParkingSpaceService adminParkingSpaceService;

    //관리자 - 주차공간 상단 요약정보
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<ParkingSpaceSummaryResponse>> getSummary(){
        return ResponseEntity.ok(ApiResponse.success(adminParkingSpaceService.getParkingSpaceSummary()));
    }

    //관리자 - 주차공간 하단 층별 리스트
    @GetMapping
    public ResponseEntity<ApiResponse<List<ParkingSpaceListResponse>>> getFloorSpaces(@RequestParam(name = "floor") Floor floor) {
        log.info("관리자가 {}층 주차 현황을 조회합니다.",floor);
        return ResponseEntity.ok(ApiResponse.success(adminParkingSpaceService.getFloorSpaces(floor)));
    }
}
