package com.example.demo.api.admin.dashboard;

import com.example.demo.domain.dashboard.dtos.response.DashboardSummaryResponseDto;
import com.example.demo.domain.dashboard.dtos.response.DashboardUsageDetailResponseDto;
import com.example.demo.domain.dashboard.dtos.response.DashboardUsageResponseDto;
import com.example.demo.domain.dashboard.service.AdminDashboardUsageService;
import com.example.demo.domain.payment.statistics.dtos.request.DashboardRevenueRequestDto;
import com.example.demo.domain.payment.statistics.dtos.response.DashboardRevenueDetailResponseDto;
import com.example.demo.domain.payment.statistics.dtos.response.DashboardRevenueResponseDto;
import com.example.demo.domain.payment.statistics.service.DashboardRevenueStats;
import com.example.demo.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "2. 대시보드 (Dashboard)", description = "매출 현황, 요약 통계 카드, 사용량 그래프 데이터 조회 API")
@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {
    private final DashboardRevenueStats dashboardRevenueStats;
    private final AdminDashboardUsageService dashboardUsageService;

    @Operation(summary = "매출 현황 조회", description = "type(DAY/WEEK/MONTH 등)에 따른 기간별 총 매출 데이터를 반환합니다.", security = @SecurityRequirement(name = "jwtAuth"))
    @GetMapping("/revenue")
    public DashboardRevenueResponseDto getRevenue(@RequestParam("type") String type){
        return dashboardRevenueStats.getDashboardTotalRevenue(type);
    }
    @Operation(summary = "대시보드 요약 통계", description = "현재 주차 수, 오늘 입출차 수, 당일 매출 등 상단 카드 데이터를 반환합니다.", security = @SecurityRequirement(name = "jwtAuth"))
    // 상단 통계카드
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<DashboardSummaryResponseDto>> getSummary(){
        return ResponseEntity.ok(
                ApiResponse.success(dashboardUsageService.getSummary())
        );
    }
    @Operation(summary = "사용량 현황 그래프", description = "type(PARKING/RESERVATION 등)별 일별 사용량 그래프 데이터를 반환합니다.", security = @SecurityRequirement(name = "jwtAuth"))
    //사용량 현황 그래프
    @GetMapping("/usage")
    public ResponseEntity<ApiResponse<DashboardUsageResponseDto>> getUsage(
            @RequestParam(defaultValue = "PARKING") String type
    ){
        return ResponseEntity.ok(
                ApiResponse.success(dashboardUsageService.getUsage(type))
        );
    }
    @Operation(summary = "사용량 상세 목록", description = "type별 사용량 상세 건별 목록을 페이징 조회합니다.", security = @SecurityRequirement(name = "jwtAuth"))
    @GetMapping("/usage/detail")
    public ResponseEntity<ApiResponse<DashboardUsageDetailResponseDto>> getUsageDetail(
            @RequestParam(defaultValue = "PARKING") String type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ){
        return ResponseEntity.ok(
                ApiResponse.success(dashboardUsageService.getUsageDetail(type, page, size))
        );
    }

    @Operation(summary = "매출 상세 조회", description = "날짜 범위 및 조회 단위로 일별 매출 상세 데이터를 반환합니다.", security = @SecurityRequirement(name = "jwtAuth"))
    @GetMapping("/revenue/detail")
    public DashboardRevenueDetailResponseDto getRevenueDetail(DashboardRevenueRequestDto dto){
        return dashboardRevenueStats.getDashboardDailyRevenueDetails(dto);
    }
}
