package com.example.demo.domain.user.vehicle.controller;

import com.example.demo.domain.user.vehicle.dtos.response.VehicleOcrResultDto;
import com.example.demo.domain.user.vehicle.dtos.response.IdCardOcrResultDto;
import com.example.demo.domain.user.vehicle.service.NaverOcrService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/api/user/ai/naver")
@RequiredArgsConstructor
public class NaverOcrController {

    private final NaverOcrService naverOcrService;

    /**
     * [1. 자동차 등록증 인증 전용]
     * 리턴 타입: VehicleOcrResultDto (차번호, 모델명, 이름, 생년월일)
     */
    @PostMapping("/upload-registration")
    public ResponseEntity<VehicleOcrResultDto> uploadRegistration(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) return ResponseEntity.badRequest().build();

        VehicleOcrResultDto result = naverOcrService.executeCarRegistrationOcr(file);
        return ResponseEntity.ok(result);
    }

    /**
     * [2. 신분증 인증 전용]
     * 리턴 타입: IdCardOcrResultDto (이름, 생년월일)
     */
    @PostMapping("/upload-idcard")
    public ResponseEntity<IdCardOcrResultDto> uploadIdCard(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) return ResponseEntity.badRequest().build();

        IdCardOcrResultDto result = naverOcrService.executeIdCardOcr(file);
        return ResponseEntity.ok(result);
    }

}
