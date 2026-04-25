package com.example.demo.api.admin.reservation;

import com.example.demo.domain.auth.admin.dtos.request.ReservationPolicyRequestDto;
import com.example.demo.domain.auth.admin.dtos.response.ReservationPolicyResponseDto;
import com.example.demo.domain.reservation.policy.service.AdminReservationPolicyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/reservation-policy")
@RequiredArgsConstructor
public class AdminReservationPolicyController {
    private final AdminReservationPolicyService adminReservationPolicyService;

    @GetMapping
    public ResponseEntity<Page<ReservationPolicyResponseDto>> getPolicies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ){
        return ResponseEntity.ok(
                adminReservationPolicyService.getPolicies(PageRequest.of(page, size)));
    }
    @GetMapping("/active")
    public ResponseEntity<ReservationPolicyResponseDto> getActivePolicy(){
        return ResponseEntity.ok(adminReservationPolicyService.getActivePolicy());
    }
    @PostMapping
    public ResponseEntity<ReservationPolicyResponseDto> createPolicy(
            @RequestBody @Valid ReservationPolicyRequestDto dto
            ){
        return ResponseEntity.ok(adminReservationPolicyService.createPolicy(dto));
    }
    @PutMapping("/{policyId}")
    public ResponseEntity<ReservationPolicyResponseDto> updatePolicy(
            @PathVariable Long policyId,
            @RequestBody @Valid ReservationPolicyRequestDto dto){
        return ResponseEntity.ok(adminReservationPolicyService.updatePolicy(policyId,dto));
    }
}
