package com.example.demo.domain.user.report.controller;

import com.example.demo.domain.user.report.dto.ReportResponseDto;
import com.example.demo.domain.user.report.entity.Report;
import com.example.demo.domain.user.report.entity.ReportType;
import com.example.demo.domain.user.report.service.ReportService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
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
            @RequestParam Long userId,
            @RequestParam String carNumber,
            @RequestParam ReportType reportType,
            @RequestParam(required = false) String description
            ){
        reportService.createReport(userId,carNumber,reportType,description);
    }

    //내가 신고한 내역
    @GetMapping("/my")
    public Page<ReportResponseDto> myReports(@RequestParam Long userId, Pageable pageable ){
        return reportService.getMyReports(userId,pageable);
    }

    //내가 받은 신고
    @GetMapping("/received")
    public Page<Report> receivedReports( @RequestParam Long userId, Pageable pageable){
        return reportService.getReceivedReports(userId,pageable);
    }

    //신고 취소
    @PatchMapping("/{reportId}/cancel")
    public void cancel( @PathVariable Long reportId, @RequestParam Long userId){
        reportService.cancelReport(reportId,userId);
    }

    //기간 검색
    @GetMapping("/search")
    public Page<Report>search(
            @RequestParam Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            Pageable pageable){
        return reportService.searchReports(userId, startDate,endDate,pageable);
    }
}
