package com.example.demo.domain.user.vehicle.controller;

import com.example.demo.domain.user.auth.principal.PrincipalDetails;
import com.example.demo.domain.user.vehicle.dtos.request.VehicleCancelRequestDto;
import com.example.demo.domain.user.vehicle.dtos.request.VehicleRegistrationRequestDto;
import com.example.demo.domain.user.vehicle.dtos.response.VehicleResponseDto;
import com.example.demo.domain.user.vehicle.service.VehicleRegistrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user/vehicles")
public class VehicleRegistrationController {

    private final VehicleRegistrationService vehicleRegistrationService;

    /**
     * [차량 등록 API]
     * - @Valid를 통해 DTO의 @NotBlank 및 OCR 데이터 유무를 1차 검증합니다.
     * - 서비스 단에서 3중 보안 검증(수정여부, 명의유사도)을 거쳐 ACTIVE/PENDING 상태를 결정합니다.
     */
    @PostMapping("/register")
    public ResponseEntity<String> registerVehicle(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @Valid @RequestBody VehicleRegistrationRequestDto requestDto) { // ⭐ @Valid 추가

        log.info("--- [Controller] 차량 등록 요청: UserID={}, CarNumber={} ---",
                principalDetails.getUserId(), requestDto.getCarNumber());

        vehicleRegistrationService.registerVehicle(principalDetails.getUserId(), requestDto);

        return ResponseEntity.ok("차량 등록 요청이 정상적으로 처리되었습니다.");
    }

    /**
     * [내 차량 정보 조회]
     */
    @GetMapping("/me")
    public ResponseEntity<VehicleResponseDto> getMyVehicle(
            @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        VehicleResponseDto response = vehicleRegistrationService.getMyVehicle(principalDetails.getUserId());

        if (response == null) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(response);
    }

    /**
     * [차량 등록 신청 취소 API]
     * - 승인 대기(PENDING) 상태인 경우에만 취소 가능
     */
    @PostMapping("/cancel")
    public ResponseEntity<String> cancelVehicleRegistration(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @Valid @RequestBody VehicleCancelRequestDto requestDto) {

        vehicleRegistrationService.cancelVehicleRegistration(principalDetails.getUserId(), requestDto);

        return ResponseEntity.ok("차량 등록 신청이 성공적으로 취소되었습니다.");
    }

    /**
     * [차량 삭제 API]
     * - 정기권 보유 여부 및 주차 현황 확인 후 처리
     */
    @DeleteMapping("/{vehicleId}")
    public ResponseEntity<String> deleteVehicle(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @PathVariable("vehicleId") Long vehicleId) {

        vehicleRegistrationService.deleteVehicle(principalDetails.getUserId(), vehicleId);
        return ResponseEntity.ok("차량이 성공적으로 삭제되었습니다.");
    }
}