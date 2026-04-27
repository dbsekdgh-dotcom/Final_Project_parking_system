package com.example.demo.domain.dashboard.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class DashboardUsageDetailResponseDto {
    private List<DetailRow> content;
    private int totalPages;
    private long totalElements;

    @Getter
    @AllArgsConstructor
    public static class DetailRow{
        private String date;
        private String category;
        private long usageCount;
        private long transactionCount;
    }
}
