package com.example.demo.domain.user.vehicle.controller;

import com.example.demo.domain.user.auth.principal.PrincipalDetails;
import com.example.demo.domain.user.vehicle.dtos.request.VehicleCancelRequestDto;
import com.example.demo.domain.user.vehicle.dtos.request.VehicleRegistrationRequestDto;
import com.example.demo.domain.user.vehicle.dtos.response.VehicleResponseDto;
import com.example.demo.domain.user.vehicle.service.VehicleRegistrationService;
import jakarta.validation.Valid;
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
    /**
     * [차량 등록 신청 취소 API]
     * - 승인 대기 중인 차량 등록 신청을 취소하고 소유권을 해제합니다.
     */
    @PostMapping("/cancel")
    public ResponseEntity<String> cancelVehicleRegistration(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @Valid @RequestBody VehicleCancelRequestDto requestDto) {

        vehicleRegistrationService.cancelVehicleRegistration(principalDetails.getUser().getUserId(), requestDto);

        return ResponseEntity.ok("차랑 등록 신청이 성공적으로 취소되었습니다.");
    }
    /**
     * [차량 삭제 API]
     * 활성화된 차량을 삭제(Soft Delete)합니다.
     * 주차 중이거나 정기권이 있는 경우 서비스 레이어에서 예외를 던집니다.
     */
    @DeleteMapping("/{vehicleId}")
    public ResponseEntity<String> deleteVehicle(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @PathVariable("vehicleId") Long vehicleId) {

        vehicleRegistrationService.deleteVehicle(principalDetails.getUser().getUserId(), vehicleId);
        return ResponseEntity.ok("차량이 성공적으로 삭제되었습니다.");
    }
}