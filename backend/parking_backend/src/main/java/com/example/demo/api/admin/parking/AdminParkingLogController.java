package com.example.demo.api.admin.parking;

import com.example.demo.domain.auth.admin.entity.Admin;
import com.example.demo.domain.parking.log.dtos.request.DiscountModifyRequest;
import com.example.demo.domain.parking.log.dtos.request.ForceExitRequest;
import com.example.demo.domain.parking.log.dtos.response.AdminTicketPolicyResponse;
import com.example.demo.domain.parking.log.service.AdminParkingService;
import com.example.demo.domain.parking.log.dtos.response.ParkingLogDetailResponse;
import com.example.demo.domain.parking.log.dtos.response.ParkingLogListResponse;
import com.example.demo.domain.parking.log.dtos.response.ParkingLogSummaryResponse;
import com.example.demo.domain.parking.log.service.ParkingLogService;
import com.example.demo.global.common.ApiResponse;
import com.example.demo.global.security.admin.AdminAuthDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "4. 입출차 관리 (Parking Log)", description = "입출차 요약 조회, 목록/상세 조회, 강제 출차, 할인 수정 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
@Log4j2
public class AdminParkingLogController {
    private final ParkingLogService parkingLogService;
    private final AdminParkingService adminParkingService;


    @Operation(summary = "입출차 요약 조회", description = "현재 주차 중, 오늘 입차·출차 수 등 상단 요약 카드 데이터를 반환합니다.", security = @SecurityRequirement(name = "jwtAuth"))
    //관리자 입출차기록 상단 요약정보 조회
    @GetMapping("/parking/summary")
    public ResponseEntity<ParkingLogSummaryResponse> getParkingSummary(){
        ParkingLogSummaryResponse summary = parkingLogService.getMainSummary();

        return ResponseEntity.ok(summary);
    }

    @Operation(summary = "입출차 목록 조회", description = "키워드·상태(ALL/ENTERED/EXITED 등) 필터와 페이징을 지원합니다.", security = @SecurityRequirement(name = "jwtAuth"))
    //관리자 입출차기록 하단 내역 테이블 조회 + 페이징
    @GetMapping("/parking/logs")
    public ResponseEntity<Page<ParkingLogListResponse>> getParkingLogList(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "ALL")String status,
            @PageableDefault(size = 5, sort = "parkingLogId", direction = Sort.Direction.DESC) Pageable pageable){
        Page<ParkingLogListResponse> response = parkingLogService.getParkingLogList(keyword,status,pageable);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "입출차 상세 조회", description = "특정 주차 로그의 차량번호, 입출차 시각, 요금, 할인 내역 등을 반환합니다.", security = @SecurityRequirement(name = "jwtAuth"))
    //특정 입출차 기록 상세 조회
    @GetMapping("/parking/logs/{parkingLogId}")
    public ResponseEntity<ParkingLogDetailResponse> getParkingLogDetail(@PathVariable Long parkingLogId){
        ParkingLogDetailResponse response = parkingLogService.getParkingLogDetail(parkingLogId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "강제 출차 처리", description = "관리자가 주차 중인 차량을 강제로 출차 처리합니다. 처리 사유를 필수로 입력해야 합니다.", security = @SecurityRequirement(name = "jwtAuth"))
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

    @Operation(summary = "할인 수정", description = "관리자가 특정 주차 로그에 적용된 할인권 정책을 변경합니다. 사유 필수 입력.", security = @SecurityRequirement(name = "jwtAuth"))
    //주차 로그 할인 수정 처리
    @PatchMapping("/parking/logs/{parkingLogId}/discount")
    public ResponseEntity<ApiResponse<Void>> modifyDiscount(
            @PathVariable Long parkingLogId,
            @Valid @RequestBody DiscountModifyRequest request,
            @AuthenticationPrincipal AdminAuthDto currentAdmin
            ) throws Exception{
        log.info("관리자[{}]가 주차로그[{}]의 할인을 수정을 시도합니다. 사유:{}"
        ,currentAdmin.getName(), parkingLogId, request.getReason());

        adminParkingService.modifyParkingDiscount(parkingLogId, request.getTicketPolicyId(), request.getReason(),currentAdmin);
        return ResponseEntity.ok(ApiResponse.success("할인 수정이 성공적으로 완료되었습니다."));
    }

    @Operation(summary = "관리자용 할인 정책 목록", description = "관리자가 할인 수정 시 선택할 수 있는 활성 할인권 정책 목록을 반환합니다.", security = @SecurityRequirement(name = "jwtAuth"))
    @GetMapping("/parking/ticket-policies/admin")
    public ResponseEntity<ApiResponse<List<AdminTicketPolicyResponse>>> getAdminPolicies(){
        log.info("관리자가 적용 가능한 할인 정책 목록을 조회합니다.");
        List<AdminTicketPolicyResponse> policies = adminParkingService.getAdminTicketPolicies();
        return ResponseEntity.ok(ApiResponse.success(policies));
    }
}
