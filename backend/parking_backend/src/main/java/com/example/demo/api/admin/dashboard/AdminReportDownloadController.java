package com.example.demo.api.admin.dashboard;

import com.example.demo.domain.dashboard.service.AdminReportDownloadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "18. 운영 보고서 (Report Download)", description = "운영 보고서 Excel 다운로드 API")
@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class AdminReportDownloadController {
    private final AdminReportDownloadService adminReportDownloadService;

    @Operation(summary = "운영 보고서 생성(Excel)", description = "period(MONTHLY/WEEKLY)와 날짜 범위를 지정하면 AI가 분석 코멘트를 삽입한 Excel 보고서를 생성해 다운로드합니다.", security = @SecurityRequirement(name = "jwtAuth"))
    @GetMapping("/report/generate")
    public ResponseEntity<byte[]> generateReport(
            @RequestParam(defaultValue = "MONTHLY") String period,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate
    ) {
        byte[] excelBytes = adminReportDownloadService.generateReport(period,startDate,endDate);

        String periodLabel="MONTHLY".equals(period)?"monthly":"weekly";
        String filename="parking_report_" + periodLabel + ".xlsx";

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename="+filename)
                .body(excelBytes);
    }
}
