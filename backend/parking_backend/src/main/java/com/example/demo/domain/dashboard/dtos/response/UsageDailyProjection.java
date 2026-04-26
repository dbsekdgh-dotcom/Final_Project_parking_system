package com.example.demo.domain.dashboard.dtos.response;

public interface UsageDailyProjection {
    String getDate();
    String getCategory();
    Long getUsageCount();
    Long getTracsactionCount();
}
