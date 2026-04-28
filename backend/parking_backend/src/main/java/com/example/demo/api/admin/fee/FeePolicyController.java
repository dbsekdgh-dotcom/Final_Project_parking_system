package com.example.demo.api.admin.fee;

import com.example.demo.domain.parking.policy.dtos.request.ParkingFeePolicyChangeRequestDto;
import com.example.demo.domain.parking.policy.dtos.request.ParkingFeePolicyUpdateRequestDto;
import com.example.demo.domain.parking.policy.service.ParkingFeePolicyService;
import com.example.demo.domain.parking.policy.service.PolicyHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "5. 요금 정책 (Fee Policy)", description = "주차 요금 정책 조회, 변경, 전체 수정, 이력 조회 API")
@Slf4j
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class FeePolicyController {
    private final ParkingFeePolicyService parkingFeePolicyService;
    private final PolicyHistoryService policyHistoryService;

    @Operation(summary = "현행 요금 정책 조회", description = "현재 적용 중인 주차 요금 정책(기본 요금, 추가 요금 단위 등)을 반환합니다.", security = @SecurityRequirement(name = "jwtAuth"))
    @GetMapping("/fee-policy")
    public Map<String,Object> searchFeePolicy(){
        return parkingFeePolicyService.getEffectiveParkingFeePolicy();
    }

    @Operation(summary = "요금 정책 변경 (새 정책 등록)", description = "기존 정책을 종료하고 새로운 요금 정책을 등록합니다.", security = @SecurityRequirement(name = "jwtAuth"))
    @PostMapping("/fee-policy/change")
    public ResponseEntity<Long> changeFeePolicy(@RequestBody ParkingFeePolicyChangeRequestDto dto){
        System.out.println("수정 요청 정책==>"+dto);
        long policyId=parkingFeePolicyService.changeParkingFeePolicy(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(policyId);
    }

    @Operation(summary = "현행 요금 정책 전체 수정", description = "현재 적용 중인 정책의 모든 필드를 일괄 수정합니다.", security = @SecurityRequirement(name = "jwtAuth"))
    @PostMapping("/fee-policy/update")
    public ResponseEntity<Long> updateFeePolicy(@RequestBody ParkingFeePolicyUpdateRequestDto dto){
        System.out.println("전체 수정 요청 정책==>"+dto);
        long policyId=parkingFeePolicyService.updateParkingFeePolicy(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(policyId);
    }

    @Operation(summary = "요금 정책 이력 조회", description = "과거부터 현재까지 등록된 모든 요금 정책 변경 이력을 반환합니다.", security = @SecurityRequirement(name = "jwtAuth"))
    @GetMapping("/fee-policy/history")
    public Map<String,Object> searchFeePolicyHistory(){return policyHistoryService.getPolicyHistory();}
}
