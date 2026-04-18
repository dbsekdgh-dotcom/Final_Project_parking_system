package com.example.demo.api.user.vehicle;

import com.example.demo.domain.auth.user.principal.PrincipalDetails;
import com.example.demo.domain.vehicle.dtos.request.VehicleCancelRequestDto;
import com.example.demo.domain.vehicle.dtos.request.VehicleRegistrationRequestDto;
import com.example.demo.domain.vehicle.dtos.response.VehicleResponseDto;
import com.example.demo.domain.vehicle.service.VehicleRegistrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "4. 차량 등록 (Vehicle)", description = "차량 등록 신청, 조회, 취소 및 삭제 API. OCR 3중 보안 검증을 통해 자동 승인 또는 관리자 검토 대기 처리됩니다.")
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user/vehicles")
public class VehicleRegistrationController {

    private final VehicleRegistrationService vehicleRegistrationService;

    @Operation(summary = "차량 등록 신청", description = "OCR로 인식한 차량등록증·신분증 데이터를 포함해 차량 등록을 신청합니다. 이름·차량번호·차종·생년월일 무수정 및 명의 유사도 조건을 모두 만족하면 자동 승인(ACTIVE), 하나라도 미달 시 관리자 검토 대기(PENDING)로 처리됩니다.", security = @SecurityRequirement(name = "jwtAuth"))
    @PostMapping("/register")
    public ResponseEntity<String> registerVehicle(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @Valid @RequestBody VehicleRegistrationRequestDto requestDto) { // ⭐ @Valid 추가

        log.info("--- [Controller] 차량 등록 요청: UserID={}, CarNumber={} ---",
                principalDetails.getUserId(), requestDto.getCarNumber());

        vehicleRegistrationService.registerVehicle(principalDetails.getUserId(), requestDto);

        return ResponseEntity.ok("차량 등록 요청이 정상적으로 처리되었습니다.");
    }

    @Operation(summary = "내 차량 정보 조회", description = "현재 로그인한 사용자의 차량 정보(차량번호, 차종, 상태)를 반환합니다. 등록된 차량이 없으면 204 No Content를 반환합니다.", security = @SecurityRequirement(name = "jwtAuth"))
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

    @Operation(summary = "차량 등록 신청 취소", description = "승인 대기(PENDING) 상태인 차량 등록 신청을 취소합니다. 이미 승인(ACTIVE)된 차량은 취소 불가합니다.", security = @SecurityRequirement(name = "jwtAuth"))
    @PostMapping("/cancel")
    public ResponseEntity<String> cancelVehicleRegistration(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @Valid @RequestBody VehicleCancelRequestDto requestDto) {

        vehicleRegistrationService.cancelVehicleRegistration(principalDetails.getUserId(), requestDto);

        return ResponseEntity.ok("차량 등록 신청이 성공적으로 취소되었습니다.");
    }

    @Operation(summary = "차량 삭제", description = "등록된 차량을 소프트 삭제합니다. 현재 주차 중이거나 유효한 정기권이 있는 경우 삭제가 불가합니다.", security = @SecurityRequirement(name = "jwtAuth"))
    @DeleteMapping("/{vehicleId}")
    public ResponseEntity<String> deleteVehicle(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @PathVariable("vehicleId") Long vehicleId) {

        vehicleRegistrationService.deleteVehicle(principalDetails.getUserId(), vehicleId);
        return ResponseEntity.ok("차량이 성공적으로 삭제되었습니다.");
    }
}