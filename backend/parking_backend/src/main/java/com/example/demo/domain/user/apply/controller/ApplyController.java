package com.example.demo.domain.user.apply.controller;

import com.example.demo.domain.user.apply.dtos.request.ResidentApplyRequestDto;
import com.example.demo.domain.user.apply.dtos.response.ResidentApplyResponseDto;
import com.example.demo.domain.user.apply.dtos.response.AvailableUnitResponseDto;
import com.example.demo.domain.user.apply.dtos.response.HouseholdListResponseDto;
import com.example.demo.domain.user.apply.service.ResidentApplyService;
import com.example.demo.domain.user.apply.service.HouseholdFindService;
import com.example.demo.domain.user.auth.principal.PrincipalDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user/apply")
@RequiredArgsConstructor
public class ApplyController {

    private final HouseholdFindService householdFindService;
    private final ResidentApplyService residentApplyService;

    // 1. 비어있는 세대 전체 정보 조회
    @GetMapping("/households")
    public ResponseEntity<List<HouseholdListResponseDto>> getEmptyHouseholds() {
        return ResponseEntity.ok(householdFindService.findAllActiveHousehold());
    }

    // 2. 신청 가능한 호수 번호 목록만 조회
    @GetMapping("/available-units")
    public ResponseEntity<AvailableUnitResponseDto> getAvailableUnits() {
        return ResponseEntity.ok(householdFindService.getAvailableUnitNos());
    }

    // 3. 입주민 신청 (POST)
    @PostMapping("/resident")
    public ResponseEntity<ResidentApplyResponseDto> applyResident(
            @Valid @RequestBody ResidentApplyRequestDto residentApplyRequestDto, // 이름 일치
            @AuthenticationPrincipal PrincipalDetails principalDetails) {

        Long userId = principalDetails.getUserId();

        // 서비스의 반환 타입인 ResidentApplyResponseDto를 그대로 반환
        return ResponseEntity.ok(residentApplyService.apply(userId, residentApplyRequestDto));
    }

}
