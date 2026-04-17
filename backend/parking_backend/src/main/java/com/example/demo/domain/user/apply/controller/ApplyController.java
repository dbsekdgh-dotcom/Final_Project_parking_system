package com.example.demo.domain.user.apply.controller;

import com.example.demo.domain.user.apply.dtos.request.ResidentApplyRequestDto;
import com.example.demo.domain.user.apply.dtos.response.*;
import com.example.demo.domain.user.apply.service.ResidentApplyService;
import com.example.demo.domain.user.apply.service.HouseholdFindService;
import com.example.demo.domain.user.auth.principal.PrincipalDetails;
import com.example.demo.global.exception.CustomException;
import com.example.demo.global.exception.ErrorCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;

@Tag(name = "3. 입주 신청 (Apply)", description = "입주민 세대 신청, 취소 및 신청 상태 조회 API")
@RestController
@RequestMapping("/api/user/apply")
@RequiredArgsConstructor
public class ApplyController {

    private final HouseholdFindService householdFindService;
    private final ResidentApplyService residentApplyService;

    @Operation(summary = "신청 가능 세대 전체 조회", description = "입주 신청이 가능한 비어있는 세대 목록 전체를 반환합니다.", security = @SecurityRequirement(name = "jwtAuth"))
    @GetMapping("/households")
    public ResponseEntity<List<HouseholdListResponseDto>> getEmptyHouseholds() {
        return ResponseEntity.ok(householdFindService.findAllActiveHouseholds());
    }

    @Operation(summary = "신청 가능 호수 번호 목록 조회", description = "드롭다운 선택용 입주 신청 가능한 호수 번호만 간략히 반환합니다.", security = @SecurityRequirement(name = "jwtAuth"))
    @GetMapping("/available-units")
    public ResponseEntity<AvailableUnitResponseDto> getAvailableUnits() {
        return ResponseEntity.ok(householdFindService.getAvailableUnitNos());
    }

    @Operation(summary = "전체 호수 상태 조회", description = "세대 선택 그리드 렌더링용. 모든 호수의 입주 가능/불가 상태를 반환합니다.", security = @SecurityRequirement(name = "jwtAuth"))
    @GetMapping("/unit-status")
    public ResponseEntity<List<UnitStatusResponseDto>> getAllUnitStatuses() { // List<> 추가
        return ResponseEntity.ok(householdFindService.getAllUnitStatuses());
    }

    @Operation(summary = "내 입주 신청 상태 조회", description = "현재 로그인한 사용자의 입주 상태를 반환합니다. (RESIDENT/PENDING/NONE)", security = @SecurityRequirement(name = "jwtAuth"))
    @GetMapping("/status")
    public ResponseEntity<UserStatusResponseDto> getUserStatus(@AuthenticationPrincipal PrincipalDetails principalDetails) {
        if (principalDetails == null) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }
        // 안전하게 userId를 꺼내옵니다.
        Long userId = principalDetails.getUserId();

        UserStatusResponseDto response = residentApplyService.getUserStatus(userId);
        return ResponseEntity.ok(response);
    }


    @Operation(summary = "입주민 신청", description = "선택한 세대에 입주민 등록 신청을 합니다. 관리자 승인 후 RESIDENT 상태로 전환됩니다.", security = @SecurityRequirement(name = "jwtAuth"))
    @PostMapping("/resident")
    public ResponseEntity<ResidentApplyResponseDto> applyResident(
            @Valid @RequestBody ResidentApplyRequestDto residentApplyRequestDto, // 이름 일치
            @AuthenticationPrincipal PrincipalDetails principalDetails) {

        Long userId = principalDetails.getUserId();

        // 서비스의 반환 타입인 ResidentApplyResponseDto를 그대로 반환
        return ResponseEntity.ok(residentApplyService.apply(userId, residentApplyRequestDto));
    }
    @Operation(summary = "입주민 신청 취소", description = "대기(PENDING) 상태인 입주 신청을 취소합니다. 이미 승인/거절된 신청은 취소 불가합니다.", security = @SecurityRequirement(name = "jwtAuth"))
    @PatchMapping("/resident/{approvalId}/cancel")
    public ResponseEntity<ResidentApplyCancelResponseDto> cancelResidentApply(
            @PathVariable Long approvalId,
            @AuthenticationPrincipal PrincipalDetails principalDetails) {

        Long userId = principalDetails.getUserId();

        return ResponseEntity.ok(residentApplyService.cancel(userId, approvalId));
    }

}