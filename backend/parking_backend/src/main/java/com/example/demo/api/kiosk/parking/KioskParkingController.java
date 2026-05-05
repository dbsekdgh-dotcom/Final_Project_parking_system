package com.example.demo.api.kiosk.parking;

import com.example.demo.domain.parking.space.dtos.response.ParkingSpaceSummaryResponse;
import com.example.demo.domain.parking.space.service.AdminParkingSpaceService;
import com.example.demo.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/kiosk")
public class KioskParkingController {

    private final AdminParkingSpaceService adminParkingSpaceService;

    @GetMapping("/parking-summary")
    public ResponseEntity<ApiResponse<ParkingSpaceSummaryResponse>> getParkingSummary(){
        return ResponseEntity.ok(ApiResponse.success(adminParkingSpaceService.getParkingSpaceSummary()));
    }
}
