package com.example.demo.domain.approval.dtos.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class AppprovalPageResponseDto {
    private List<ApprovalResponseDto> content;
    private int totalPages;
    private long totalElements;
    private int number;
    private ApprovalStatsDto stats;
}
