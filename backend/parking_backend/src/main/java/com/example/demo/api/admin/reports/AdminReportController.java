package com.example.demo.api.admin.reports;

import com.example.demo.domain.report.dto.request.AdminReportRejectRequestDto;
import com.example.demo.domain.report.dto.response.AdminReportPageResponseDto;
import com.example.demo.domain.report.entity.ReportStatus;
import com.example.demo.domain.report.entity.ReportType;
import com.example.demo.domain.report.service.AdminReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "9. 신고 관리 (Report)", description = "사용자 신고 목록 조회, 승인(블랙리스트 등록), 거절 API")
@RestController
@RequestMapping("/api/admin/reports")
@RequiredArgsConstructor
public class AdminReportController {
    private final AdminReportService adminReportService;

    @Operation(summary = "신고 목록 조회", description = "신고 유형·상태·키워드 필터로 신고 목록을 페이징 조회합니다.", security = @SecurityRequirement(name = "jwtAuth"))
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
    @Operation(summary = "신고 승인", description = "신고를 승인합니다. 승인 시 해당 차량이 블랙리스트에 등록됩니다.", security = @SecurityRequirement(name = "jwtAuth"))
    @PostMapping("/{reportId}/approve")
    public ResponseEntity<Void> approve(@PathVariable Long reportId){
        adminReportService.approve(reportId);
        return ResponseEntity.ok().build();
    }
    @Operation(summary = "신고 거절", description = "신고를 거절합니다. 거절 사유를 body에 포함해야 합니다.", security = @SecurityRequirement(name = "jwtAuth"))
    @PostMapping("/{reportId}/reject")
    public ResponseEntity<Void> reject(
            @PathVariable Long reportId,
            @RequestBody AdminReportRejectRequestDto dto
            ){
        adminReportService.reject(reportId, dto.getRejectReason());
        return ResponseEntity.ok().build();
    }
}
