package com.example.demo.api.admin.reports;

import com.example.demo.domain.report.dto.request.AdminReportRejectRequestDto;
import com.example.demo.domain.report.dto.response.AdminReportPageResponseDto;
import com.example.demo.domain.report.entity.ReportStatus;
import com.example.demo.domain.report.entity.ReportType;
import com.example.demo.domain.report.service.AdminReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/reports")
@RequiredArgsConstructor
public class AdminReportController {
    private final AdminReportService adminReportService;

    @GetMapping
    public ResponseEntity<AdminReportPageResponseDto> getReports(
            @RequestParam(required = false)ReportType type,
            @RequestParam(required = false)ReportStatus status,
            @RequestParam(required = false)String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
            ){
        return ResponseEntity.ok(
                adminReportService.getReports(type, status, keyword, PageRequest.of(page,size))
        );
    }
    @PostMapping("/{reportId}/approve")
    public ResponseEntity<Void> approve(@PathVariable Long reportId){
        adminReportService.approve(reportId);
        return ResponseEntity.ok().build();
    }
    @PostMapping("/{reportId}/reject")
    public ResponseEntity<Void> reject(
            @PathVariable Long reportId,
            @RequestBody AdminReportRejectRequestDto dto
            ){
        adminReportService.reject(reportId, dto.getRejectReason());
        return ResponseEntity.ok().build();
    }
}
