package com.example.demo.domain.dashboard.service;

import com.example.demo.domain.dashboard.dtos.response.DashboardSummaryResponseDto;
import com.example.demo.domain.dashboard.dtos.response.DashboardUsageResponseDto;
import com.example.demo.domain.payment.statistics.dtos.response.DashboardRevenueResponseDto;
import com.example.demo.domain.payment.statistics.service.DashboardRevenueStats;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminReportDownloadService {
    private final AdminDashboardUsageService dashboardUsageService;
    private final DashboardRevenueStats dashboardRevenueStats;
    private final RestTemplate restTemplate;

    @Value("${AI_SERVER_URL}")
    private String aiServerUrl;

    public byte[] generateReport(String period, String startDate, String endDate){
        //날짜 기본값(미입력시 이번달 1일~오늘)
        DateTimeFormatter fmt=DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate now = LocalDate.now();
        String start = (startDate!=null) ? startDate : now.withDayOfMonth(1).format(fmt);
        String end = (endDate!=null) ? endDate : now.format(fmt);

        // 요약통계
        DashboardSummaryResponseDto summary = dashboardUsageService.getSummary();
        // 매출현황
        DashboardRevenueResponseDto revenue = dashboardRevenueStats.getDashboardTotalRevenue("MONTH");
        //사용량 현황
        DashboardUsageResponseDto usage = dashboardUsageService.getUsage("PARKING");
        //Python data dict 조립
        Map<String,Object> data = new HashMap<>();
        data.put("household_count",summary.getHouseholdCount());
        data.put("total_household_count",summary.getTotalHouseholdCount());
        data.put("vehicle_count",summary.getVehicleCount());
        data.put("occupied_space",summary.getOccupiedParkingSpaceCount());
        data.put("total_space",summary.getParkingSpaceCount());
        data.put("total_revenue",summary.getTotalRevenue());
        data.put("revenue_total",revenue.getTotalAmount());
        data.put("revenue_change_percent",revenue.getChangePercent());
        data.put("usage_total_count", usage.getTotalCount());
        data.put("usage_change_percent",usage.getChangePercent());

        //월별 매출 리스트
        List<Map<String,Object>> revenueMonthly = revenue.getMonthly().stream()
                .map(m -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("month",m.getMonth());
                    item.put("amount",m.getAmount());
                    return item;
                }).toList();
        data.put("revenue_monthly",revenueMonthly);

        //Python 서비스 호출
        Map<String, Object> requestBody=new HashMap<>();
        requestBody.put("data",data);
        requestBody.put("period", period);
        requestBody.put("start_date",start);
        requestBody.put("end_date",end);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // RestTemplate으로 Python 호출
        ResponseEntity<byte[]> response=restTemplate.exchange(
                aiServerUrl + "/api/v1/parking/report/generate",
                HttpMethod.POST,
                new HttpEntity<>(requestBody, headers),
                byte[].class  // ← 응답을 바이너리(xlsx)로 받겠다
        );
        return response.getBody();
    }
}
