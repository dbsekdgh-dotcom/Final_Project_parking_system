package com.example.demo.domain.report.dto.response;

import com.example.demo.domain.report.entity.Report;
import com.example.demo.domain.report.entity.ReportStatus;
import com.example.demo.domain.report.entity.ReportType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AdminReportResponseDto {
    private Long reportId;
    private ReportType reportType;
    private String carNumber;
    private String reporterName;
    private String description;
    private String imageUrl;
    private ReportStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;

    public static AdminReportResponseDto from(Report r){
        String name = (r.getReporter() != null) ? r.getReporter().getName() : "알 수 없음";
        return AdminReportResponseDto.builder()
                .reportId(r.getId())
                .reportType(r.getReportType())
                .carNumber(r.getCarNumber())
                .reporterName(name)
                .description(r.getDescription())
                .imageUrl(r.getImageUrl())
                .status(r.getStatus())
                .createdAt(r.getCreatedAt())
                .resolvedAt(r.getResolvedAt())
                .build();
    }
}
