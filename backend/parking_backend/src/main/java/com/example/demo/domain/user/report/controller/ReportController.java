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
@RequestMapping("/api/report")
public class ReportController {

    private final ReportService reportService;

    //신고 생성
    @PostMapping
    public void create(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @RequestParam String carNumber,
            @RequestParam ReportType reportType,
            @RequestParam(required = false) String description
            ){
        Long userId = principalDetails.getUserId();
        reportService.createReport(userId,carNumber,reportType,description);
    }

    //내가 신고한 내역
    @GetMapping("/my")
    public Page<ReportResponseDto> myReports(@AuthenticationPrincipal PrincipalDetails principalDetails, Pageable pageable ){
        Long userId = principalDetails.getUserId();
        return reportService.getMyReports(userId,pageable);
    }

    //내가 받은 신고
    @GetMapping("/received")
    public Page<ReportResponseDto> receivedReports(@AuthenticationPrincipal PrincipalDetails principalDetails, Pageable pageable){
        Long userId = principalDetails.getUserId();
        return reportService.getReceivedReports(userId,pageable)
                .map(ReportResponseDto::from);
    }

    //신고 취소
    @PatchMapping("/{reportId}/cancel")
    public void cancel(@PathVariable Long reportId, @AuthenticationPrincipal PrincipalDetails principalDetails){
        Long userId = principalDetails.getUserId();
        reportService.cancelReport(reportId,userId);
    }

    //기간 검색
    @GetMapping("/search")
    public Page<Report>search(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            Pageable pageable){
        Long userId = principalDetails.getUserId();
        return reportService.searchReports(userId, startDate,endDate,pageable);
    }

    //신고 승인관련
    @PatchMapping("/{reportId}/status")
    public ResponseEntity<String> updateStatus(
            @PathVariable Long reportId,
            @RequestParam ReportStatus status,
            @RequestParam(required = false)Long adminId){ //승인/거절시 필요!

        reportService.updateReportStatus(reportId, status, adminId);
        return ResponseEntity.ok("신고 상태가 " + status + "로 변경되었습니다.");

    }

}
