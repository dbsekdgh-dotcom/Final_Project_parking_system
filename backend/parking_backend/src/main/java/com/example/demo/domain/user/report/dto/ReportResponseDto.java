package com.example.demo.domain.user.report.dto;

import com.example.demo.domain.user.report.entity.Report;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ReportResponseDto {

    private Long id;
    private String carNumber;
    private String reportType;
    private String description;
    private String reporterName;
    private LocalDateTime createdAt;

    //dto 책임을 dto 가진다.   service코드가 깔끔해지기 위해 추가. service가 dto변환까지 하면 코드가 길어지고 지져분해짐
    public static ReportResponseDto from(Report report){
        return new ReportResponseDto(
                report.getId(),
                report.getCarNumber(),
                report.getReportType().name(),
                report.getDescription(),
                report.getReporter().getName(),
                report.getCreatedAt()
        );
    }
}
