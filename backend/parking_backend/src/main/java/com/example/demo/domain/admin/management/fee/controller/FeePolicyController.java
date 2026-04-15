package com.example.demo.domain.admin.management.fee.controller;

import com.example.demo.domain.admin.management.fee.service.ParkingFeePolicyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class FeePolicyController {
    private final ParkingFeePolicyService parkingFeePolicyService;

    @GetMapping("/fee-policy")
    public Map<String,Object> searchFeePolicy(){
        Map<String,Object> map= parkingFeePolicyService.getEffectiveParkingFeePolicy();
        log.info("정책 응답 데이터===>",map);
        return map;
    }
}
