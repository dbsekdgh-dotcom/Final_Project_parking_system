package com.example.demo.domain.payment.statistics.dtos.response;

import lombok.*;

import java.util.List;

//getRevenue
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter@Getter
public class DashboardRevenueResponseDto {
    private long totalAmount;  //전체
    private double changePercent;  //전월 대비
    private List<DashboardMonthlyRevenueDto> monthly;

}
