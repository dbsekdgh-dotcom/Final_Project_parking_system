package com.example.demo.domain.admin.management.fee.controller;

import com.example.demo.domain.admin.management.fee.dtos.request.ParkingFeePolicyChangeRequestDto;
import com.example.demo.domain.admin.management.fee.service.ParkingFeePolicyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class FeePolicyController {
    private final ParkingFeePolicyService parkingFeePolicyService;

    @GetMapping("/fee-policy")
    public Map<String,Object> searchFeePolicy(){
        return parkingFeePolicyService.getEffectiveParkingFeePolicy();
    }

    @PostMapping("/fee-policy/change")
    public ResponseEntity<Long> changeFeePolicy(@RequestBody ParkingFeePolicyChangeRequestDto parkingFeePolicyChangeRequestDto){
        System.out.println("수정 요청 정책==>"+parkingFeePolicyChangeRequestDto);
        long policyId=parkingFeePolicyService.changeParkingFeePolicy(parkingFeePolicyChangeRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(policyId);
    }
}
