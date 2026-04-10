package com.example.demo.domain.user.report.controller;

import com.example.demo.domain.user.auth.principal.PrincipalDetails;
import com.example.demo.domain.user.report.dto.ReportResponseDto;
import com.example.demo.domain.user.report.entity.Report;
import com.example.demo.domain.user.report.entity.ReportStatus;
import com.example.demo.domain.user.report.entity.ReportType;
import com.example.demo.domain.user.report.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/reports")
public class ReportController {

    private final ReportService reportService;

    /**
     * 신고 생성
     * 토큰에서 이메일을 추출하여 작성자(reporter)로 설정합니다.
     */
    @PostMapping
    public void create(
            @AuthenticationPrincipal PrincipalDetails principal,
            @RequestParam String carNumber,
            @RequestParam ReportType reportType,
            @RequestParam(required = false) String description
    ){
        reportService.createReport(principal.getUsername(), carNumber, reportType, description);
    }

    /**
     * 내가 신고한 내역
     * 토큰 기반으로 본인의 신고 내역만 조회합니다.
     */
    @GetMapping("/my")
    public Page<ReportResponseDto> myReports(
            @AuthenticationPrincipal PrincipalDetails principal,
            Pageable pageable
    ){
        return reportService.getMyReports(principal.getUsername(), pageable);
    }

    /**
     * 내가 받은 신고
     * 토큰 기반으로 본인의 차량에 접수된 신고를 조회합니다.
     */
    @GetMapping("/received")
    public Page<ReportResponseDto> receivedReports(
            @AuthenticationPrincipal PrincipalDetails principal,
            Pageable pageable
    ){
        return reportService.getReceivedReports(principal.getUsername(), pageable)
                .map(ReportResponseDto::from);
    }

    /**
     * 신고 취소
     */
    @PatchMapping("/{reportId}/cancel")
    public void cancel(
            @PathVariable Long reportId,
            @AuthenticationPrincipal PrincipalDetails principal
    ){
        reportService.cancelReport(reportId, principal.getUsername());
    }

    /**
     * 기간 검색
     */
    @GetMapping("/search")
    public Page<Report> search(
            @AuthenticationPrincipal PrincipalDetails principal,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            Pageable pageable){
        return reportService.searchReports(principal.getUsername(), startDate, endDate, pageable);
    }

    /**
     * 신고 상태 변경 (관리자 전용 혹은 시스템 승인)
     * 이 부분은 adminId가 따로 파라미터로 들어오므로 유지합니다.
     */
    @PatchMapping("/{reportId}/status")
    public ResponseEntity<String> updateStatus(
            @PathVariable Long reportId,
            @RequestParam ReportStatus status,
            @RequestParam(required = false) Long adminId){

        reportService.updateReportStatus(reportId, status, adminId);
        return ResponseEntity.ok("신고 상태가 " + status + "로 변경되었습니다.");
    }
}