package com.example.demo.api.user.report;

import com.example.demo.domain.auth.user.principal.PrincipalDetails;
import com.example.demo.domain.report.dto.ReportResponseDto;
import com.example.demo.domain.report.entity.Report;
import com.example.demo.domain.report.entity.ReportStatus;
import com.example.demo.domain.report.entity.ReportType;
import com.example.demo.domain.report.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Map;

@Tag(name = "11. 신고 (Report)", description = "불법 주차 신고 접수, 내 신고 내역, 받은 신고 조회 및 신고 취소 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user/report")
public class ReportController {

    private final ReportService reportService;
    private final RestTemplate restTemplate;

    @Value("${AI_SERVER_URL}")
    private String aiServerUrl;

    @Operation(summary = "신고 이미지 AI 분석", description = "이미지를 AI 서버로 전달하여 불법 주차 여부를 분석합니다. S3 경로 및 분석 결과를 반환합니다.")
    @PostMapping("/upload")
    public ResponseEntity<Map> uploadReportImage(@RequestParam("file") MultipartFile file) throws Exception {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        ByteArrayResource resource = new ByteArrayResource(file.getBytes()) {
            @Override
            public String getFilename() { return file.getOriginalFilename(); }
        };
        body.add("file", resource);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);
        return restTemplate.postForEntity(aiServerUrl + "/api/v1/parking/report/", request, Map.class);
    }

    @Operation(summary = "신고 접수", description = "차량번호·신고 유형·설명·이미지 S3 경로를 받아 신고를 접수합니다.", security = @SecurityRequirement(name = "jwtAuth"))
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

    @Operation(summary = "내가 신고한 내역 조회", description = "로그인한 사용자가 접수한 신고 목록을 페이징 조회합니다. CANCELLED 상태 제외.", security = @SecurityRequirement(name = "jwtAuth"))
    //내가 신고한 내역
    @GetMapping("/my")
    public Page<ReportResponseDto> myReports(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            Pageable pageable
    ){
        return reportService.getMyReports(principalDetails.getUsername(), pageable);
    }

    @Operation(summary = "내가 받은 신고 조회", description = "로그인한 사용자의 차량에 접수된 신고 목록을 페이징 조회합니다.", security = @SecurityRequirement(name = "jwtAuth"))
    //내가 받은 신고
    @GetMapping("/received")
    public Page<ReportResponseDto> receivedReports(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            Pageable pageable
    ){
        return reportService.getReceivedReports(principalDetails.getUsername(), pageable);

    }

    @Operation(summary = "신고 취소", description = "본인이 접수한 신고를 취소합니다.", security = @SecurityRequirement(name = "jwtAuth"))
    //신고 취소
    @PatchMapping("/{reportId}/cancel")
    public void cancel(
            @PathVariable Long reportId,
            @AuthenticationPrincipal PrincipalDetails principalDetails
    ){
        reportService.cancelReport(reportId, principalDetails.getUsername());
    }

    @Operation(summary = "기간별 신고 검색", description = "시작일~종료일 범위로 본인의 신고 내역을 페이징 검색합니다.", security = @SecurityRequirement(name = "jwtAuth"))
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

}
