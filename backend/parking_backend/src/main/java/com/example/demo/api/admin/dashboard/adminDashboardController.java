package com.example.demo.api.admin.dashboard;

import com.example.demo.domain.payment.statistics.dtos.response.DashboardRevenueResponseDto;
import com.example.demo.domain.payment.statistics.service.DashboardRevenueStats;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class adminDashboardController {
    private final DashboardRevenueStats dashboardRevenueStats;

    @GetMapping("/revenue")
    public DashboardRevenueResponseDto getRevenue(@RequestParam("type") String type){
        return dashboardRevenueStats.getDashboardTotalRevenue(type);
    }
}
