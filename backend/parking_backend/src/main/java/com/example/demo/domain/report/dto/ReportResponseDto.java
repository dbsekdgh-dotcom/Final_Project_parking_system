package com.example.demo.domain.report.dto;

import com.example.demo.domain.report.entity.Report;
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
    private String status;

    //dto 책임을 dto 가진다.   service코드가 깔끔해지기 위해 추가. service가 dto변환까지 하면 코드가 길어지고 지져분해짐
    public static ReportResponseDto from(Report report){
        return new ReportResponseDto(
                report.getId(),
                report.getCarNumber(),
                report.getReportType()!=null? report.getReportType().name():"미지정", //Enum널 체크
                report.getDescription(),
                report.getReporter()!=null? report.getReporter().getName():"탈퇴한 사용자", //핵심!!!!
                report.getCreatedAt(),
                report.getStatus()!=null? report.getStatus().name(): "PENDING"
        );
    }
}
