package com.example.demo.domain.user.vehicle.controller;

import com.example.demo.domain.user.auth.principal.PrincipalDetails;
import com.example.demo.domain.user.vehicle.dtos.request.VehicleRegistrationRequestDto;
import com.example.demo.domain.user.vehicle.dtos.response.VehicleResponseDto;
import com.example.demo.domain.user.vehicle.service.VehicleRegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user/vehicles")
public class VehicleRegistrationController {

    private final VehicleRegistrationService vehicleRegistrationService;

    /**
     * [차량 등록 API] PrincipalDetails에서 유저 ID를 추출하고 OCR 데이터를 기반으로 차량 등록을 처리합니다.
     */
    @PostMapping("/register")
    public ResponseEntity<String> registerVehicle(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @RequestBody VehicleRegistrationRequestDto requestDto) {

        // 서비스 호출 시 Principal에서 가져온 userId와 DTO를 함께 전달
        vehicleRegistrationService.registerVehicle(principalDetails.getUserId(), requestDto);

        return ResponseEntity.ok("차량 등록 요청이 정상적으로 처리되었습니다.");
    }
    /**
     * [GET] 내 차량 정보 조회
     * - 등록된 차가 없으면 204 No Content 또는 200과 함께 null 반환
     */
    @GetMapping("/me")
    public ResponseEntity<VehicleResponseDto> getMyVehicle(
            @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {

        VehicleResponseDto response = vehicleRegistrationService.getMyVehicle(principalDetails.getUser().getUserId());

        if (response == null) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(response);
    }
}