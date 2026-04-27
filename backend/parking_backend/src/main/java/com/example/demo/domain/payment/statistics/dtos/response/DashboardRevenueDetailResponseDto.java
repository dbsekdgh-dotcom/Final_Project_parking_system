package com.example.demo.domain.payment.statistics.dtos.response;

import lombok.*;

import java.util.List;


@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
public class DashboardRevenueDetailResponseDto {
    private List<DashboardRevenueDetailDto> content;
    private int totalPages;
    private int totalElements;
}
