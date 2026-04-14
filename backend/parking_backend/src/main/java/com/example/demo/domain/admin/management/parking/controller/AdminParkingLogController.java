package com.example.demo.domain.admin.management.parking.controller;

import com.example.demo.domain.admin.entity.Admin;
import com.example.demo.domain.admin.management.parking.dtos.request.DiscountModifyRequest;
import com.example.demo.domain.admin.management.parking.dtos.request.ForceExitRequest;
import com.example.demo.domain.admin.management.parking.dtos.response.AdminTicketPolicyResponse;
import com.example.demo.domain.admin.management.parking.service.AdminParkingService;
import com.example.demo.domain.shared.parkinglog.dtos.response.ParkingLogDetailResponse;
import com.example.demo.domain.shared.parkinglog.dtos.response.ParkingLogListResponse;
import com.example.demo.domain.shared.parkinglog.dtos.response.ParkingLogSummaryResponse;
import com.example.demo.domain.shared.parkinglog.service.ParkingLogService;
import com.example.demo.global.common.ApiResponse;
import com.example.demo.global.security.admin.AdminAuthDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin")
@Log4j2
public class AdminParkingLogController {
    private final ParkingLogService parkingLogService;
    private final AdminParkingService adminParkingService;


    //관리자 입출차기록 상단 요약정보 조회
    @GetMapping("/parking/summary")
    public ResponseEntity<ParkingLogSummaryResponse> getParkingSummary(){
        ParkingLogSummaryResponse summary = parkingLogService.getMainSummary();

        return ResponseEntity.ok(summary);
    }

    //관리자 입출차기록 하단 내역 테이블 조회 + 페이징
    @GetMapping("/parking/logs")
    public ResponseEntity<Page<ParkingLogListResponse>> getParkingLogList(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "ALL")String status,
            @PageableDefault(size = 5, sort = "parkingLogId", direction = Sort.Direction.DESC) Pageable pageable){
        Page<ParkingLogListResponse> response = parkingLogService.getParkingLogList(keyword,status,pageable);
        return ResponseEntity.ok(response);
    }

    //특정 입출차 기록 상세 조회
    @GetMapping("/parking/logs/{parkingLogId}")
    public ResponseEntity<ParkingLogDetailResponse> getParkingLogDetail(@PathVariable Long parkingLogId){
        ParkingLogDetailResponse response = parkingLogService.getParkingLogDetail(parkingLogId);
        return ResponseEntity.ok(response);
    }

    //주차 로그 강제 출차 처리
    @PostMapping("/parking/logs/{parkingLogId}/force-exit")
    public ResponseEntity<ApiResponse<Void>> forceExit(
            @PathVariable Long parkingLogId,
            @RequestBody ForceExitRequest request,
            @AuthenticationPrincipal AdminAuthDto currentAdmin
            ) throws Exception{
        log.info("컨트롤러에 들어온 관리자 정보: "+currentAdmin);
        adminParkingService.processForceExit(parkingLogId,currentAdmin,request.getReason());

        return ResponseEntity.ok(ApiResponse.success("강제 출차 처리가 완료되었습니다."));
    }

    //주차 로그 할인 수정 처리
//    @PatchMapping("/parking/logs/{parkingLogId}/discount")
//    public ResponseEntity<ApiResponse<Void>> modifyDiscount(
//            @PathVariable Long parkingLogId,
//            @Valid @RequestBody DiscountModifyRequest request,
//            @AuthenticationPrincipal AdminAuthDto currentAdmin
//            ) throws Exception{
//
//        adminParkingService.modifyParkingDiscount(parkingLogId, request.getTicketPolicyId(), request.getReason(),currentAdmin);
//        return ResponseEntity.ok(ApiResponse.success("할인 수정이 성공적으로 완료되었습니다."));
//    }

    @GetMapping("/parking/ticket-policies/admin")
    public ResponseEntity<ApiResponse<List<AdminTicketPolicyResponse>>> getAdminPolicies(){
        log.info("관리자가 적용 가능한 할인 정책 목록을 조회합니다.");
        List<AdminTicketPolicyResponse> policies = adminParkingService.getAdminTicketPolicies();
        return ResponseEntity.ok(ApiResponse.success(policies));
    }
}
