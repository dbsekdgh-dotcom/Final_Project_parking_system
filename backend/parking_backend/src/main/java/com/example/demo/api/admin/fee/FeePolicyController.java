package com.example.demo.api.admin.fee;

import com.example.demo.domain.parking.policy.dtos.request.ParkingFeePolicyChangeRequestDto;
import com.example.demo.domain.parking.policy.dtos.request.ParkingFeePolicyUpdateRequestDto;
import com.example.demo.domain.parking.policy.service.ParkingFeePolicyService;
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
    public ResponseEntity<Long> changeFeePolicy(@RequestBody ParkingFeePolicyChangeRequestDto dto){
        System.out.println("수정 요청 정책==>"+dto);
        long policyId=parkingFeePolicyService.changeParkingFeePolicy(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(policyId);
    }

    @PostMapping("/fee-policy/update")
    public ResponseEntity<Long> updateFeePolicy(@RequestBody ParkingFeePolicyUpdateRequestDto dto){
        System.out.println("전체 수정 요청 정책==>"+dto);
        long policyId=parkingFeePolicyService.updateParkingFeePolicy(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(policyId);
    }

}
