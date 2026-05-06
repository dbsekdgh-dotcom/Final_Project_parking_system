package com.example.demo.api.kiosk.parking;

import com.example.demo.domain.parking.space.dtos.response.ParkingSpaceSummaryResponse;
import com.example.demo.domain.parking.space.service.AdminParkingSpaceService;
import com.example.demo.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "8. 키오스크 주차 (Kiosk Parking)", description = "키오스크용 주차장 현황 조회 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/kiosk")
public class KioskParkingController {

    private final AdminParkingSpaceService adminParkingSpaceService;

    @Operation(summary = "주차장 현황 조회", description = "키오스크에서 현재 주차장 전체 현황(총 면수, 사용 중, 가용 면수, 점유율)을 조회합니다.")
    @GetMapping("/parking-summary")
    public ResponseEntity<ApiResponse<ParkingSpaceSummaryResponse>> getParkingSummary(){
        return ResponseEntity.ok(ApiResponse.success(adminParkingSpaceService.getParkingSpaceSummary()));
    }
}
