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
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {
    private final DashboardRevenueStats dashboardRevenueStats;
    private final AdminDashboardUsageService dashboardUsageService;

    @GetMapping("/revenue")
    public DashboardRevenueResponseDto getRevenue(@RequestParam("type") String type){
        return dashboardRevenueStats.getDashboardTotalRevenue(type);
    }
    // 상단 통계카드
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<DashboardSummaryResponseDto>> getSummary(){
        return ResponseEntity.ok(
                ApiResponse.success(dashboardUsageService.getSummary())
        );
    }
    //사용량 현황 그래프
    @GetMapping("/usage")
    public ResponseEntity<ApiResponse<DashboardUsageResponseDto>> getUsage(
            @RequestParam(defaultValue = "PARKING") String type
    ){
        return ResponseEntity.ok(
                ApiResponse.success(dashboardUsageService.getUsage(type))
        );
    }
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

    @GetMapping("/revenue/detail")
    public DashboardRevenueDetailResponseDto getRevenueDetail(DashboardRevenueRequestDto dto){
        return dashboardRevenueStats.getDashboardDailyRevenueDetails(dto);
    }
}
