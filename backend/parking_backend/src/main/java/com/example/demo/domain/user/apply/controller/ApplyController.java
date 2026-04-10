package com.example.demo.domain.user.apply.controller;

import com.example.demo.domain.user.apply.dtos.request.ApprovalResidentRequestDto;
import com.example.demo.domain.user.apply.dtos.response.ApprovalResidentResponseDto;
import com.example.demo.domain.user.apply.dtos.response.AvailableUnitResponseDto;
import com.example.demo.domain.user.apply.dtos.response.HouseholdListResponseDto;
import com.example.demo.domain.user.apply.service.ApprovalResidentService;
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
    private final ApprovalResidentService approvalResidentService;

    @GetMapping("/households")
    public ResponseEntity<List<HouseholdListResponseDto>> getEmptyHouseholds() {

        return ResponseEntity.ok(householdFindService.findAllActiveHousehold());
    }

    @GetMapping("/available-units")
    public ResponseEntity<AvailableUnitResponseDto> getAvailableUnits() {
        return ResponseEntity.ok(householdFindService.getAvailableUnitNos());
    }

    @PostMapping("/resident")
    public ResponseEntity<ApprovalResidentResponseDto> applyResident(
            @Valid @RequestBody ApprovalResidentRequestDto approvalResidentRequestDto,
            @AuthenticationPrincipal PrincipalDetails principalDetails) {

        Long userId = principalDetails.getUserId();

        return ResponseEntity.ok(approvalResidentService.apply(userId, approvalResidentRequestDto));
    }

}
