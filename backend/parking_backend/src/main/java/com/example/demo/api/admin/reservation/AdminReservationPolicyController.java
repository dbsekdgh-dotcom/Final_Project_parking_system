package com.example.demo.api.admin.reservation;

import com.example.demo.domain.auth.admin.dtos.request.ReservationPolicyRequestDto;
import com.example.demo.domain.auth.admin.dtos.response.ReservationPolicyResponseDto;
import com.example.demo.domain.reservation.policy.service.AdminReservationPolicyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "12. 예약 정책 (Reservation Policy)", description = "방문 예약 정책 목록/현행 조회, 등록, 수정 API")
@RestController
@RequestMapping("/api/admin/reservation-policy")
@RequiredArgsConstructor
public class AdminReservationPolicyController {
    private final AdminReservationPolicyService adminReservationPolicyService;

    @Operation(summary = "예약 정책 목록 조회", description = "등록된 모든 예약 정책을 페이징 조회합니다.", security = @SecurityRequirement(name = "jwtAuth"))
    @GetMapping
    public ResponseEntity<Page<ReservationPolicyResponseDto>> getPolicies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ){
        return ResponseEntity.ok(
                adminReservationPolicyService.getPolicies(PageRequest.of(page, size)));
    }
    @Operation(summary = "현행 예약 정책 조회", description = "현재 적용 중인 예약 정책(최대 예약 일수, 취소 가능 시간 등)을 반환합니다.", security = @SecurityRequirement(name = "jwtAuth"))
    @GetMapping("/active")
    public ResponseEntity<ReservationPolicyResponseDto> getActivePolicy(){
        return ResponseEntity.ok(adminReservationPolicyService.getActivePolicy());
    }
    @Operation(summary = "예약 정책 등록", description = "새로운 방문 예약 정책을 등록합니다.", security = @SecurityRequirement(name = "jwtAuth"))
    @PostMapping
    public ResponseEntity<ReservationPolicyResponseDto> createPolicy(
            @RequestBody @Valid ReservationPolicyRequestDto dto
            ){
        return ResponseEntity.ok(adminReservationPolicyService.createPolicy(dto));
    }
    @Operation(summary = "예약 정책 수정", description = "지정한 예약 정책의 내용을 수정합니다.", security = @SecurityRequirement(name = "jwtAuth"))
    @PutMapping("/{policyId}")
    public ResponseEntity<ReservationPolicyResponseDto> updatePolicy(
            @PathVariable Long policyId,
            @RequestBody @Valid ReservationPolicyRequestDto dto){
        return ResponseEntity.ok(adminReservationPolicyService.updatePolicy(policyId,dto));
    }
}
