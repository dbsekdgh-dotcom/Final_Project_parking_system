package com.example.demo.api.user.report;

import com.example.demo.domain.auth.user.principal.PrincipalDetails;
import com.example.demo.domain.report.dto.ReportResponseDto;
import com.example.demo.domain.report.entity.Report;
import com.example.demo.domain.report.entity.ReportStatus;
import com.example.demo.domain.report.entity.ReportType;
import com.example.demo.domain.report.service.ReportService;
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
    public ResponseEntity<String> create(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @RequestParam String carNumber,
            @RequestParam ReportType reportType,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String report_s3path
    ){
       //로그인 체크
        if(principalDetails ==null){
            return ResponseEntity.status(401).body("로그인이 필요합니다.");
        }
        //서비스 호출
        reportService.createReport(principalDetails.getUsername(),carNumber,reportType,description, report_s3path);

        return ResponseEntity.ok("신고 접수 완료!");
    }

    //내가 신고한 내역
    @GetMapping("/my")
    public Page<ReportResponseDto> myReports(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            Pageable pageable
    ){
        return reportService.getMyReports(principalDetails.getUsername(), pageable);
    }

    //내가 받은 신고
    @GetMapping("/received")
    public Page<ReportResponseDto> receivedReports(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            Pageable pageable
    ){
        return reportService.getReceivedReports(principalDetails.getUsername(), pageable);

    }

    //신고 취소
    @PatchMapping("/{reportId}/cancel")
    public void cancel(
            @PathVariable Long reportId,
            @AuthenticationPrincipal PrincipalDetails principalDetails
    ){
        reportService.cancelReport(reportId, principalDetails.getUsername());
    }

    //기간 검색
    @GetMapping("/search")
    public Page<ReportResponseDto> search(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            Pageable pageable
    ){
        return reportService.searchReports(principalDetails.getUsername(), startDate, endDate, pageable);
    }

    //신고 승인관련 (어드민)
    @PatchMapping("/{reportId}/status")
    public ResponseEntity<String> updateStatus(
            @PathVariable Long reportId,
            @RequestParam ReportStatus status,
            @RequestParam(required = false) Long adminId){

        reportService.updateReportStatus(reportId, status, adminId);
        return ResponseEntity.ok("신고 상태가 " + status + "로 변경되었습니다.");
    }
}
