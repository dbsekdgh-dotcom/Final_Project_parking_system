package com.example.demo.api.admin.activitylog;

import com.example.demo.domain.system.activitylog.dtos.response.ActivityLogDetailDto;
import com.example.demo.domain.system.activitylog.dtos.response.ActivityLogListDto;
import com.example.demo.domain.system.activitylog.enums.ActivityType;
import com.example.demo.domain.system.activitylog.service.AdminActivityLogService;
import com.example.demo.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Tag(name = "16. 활동 로그 (Activity Log)", description = "키오스크·상가 결제 등 시스템 활동 로그 목록 및 상세 조회 API")
@RestController
@RequestMapping("/api/admin/activity-logs")
@RequiredArgsConstructor
public class AdminActivityLogController {

    private final AdminActivityLogService adminActivityLogService;

    @Operation(summary = "활동 로그 목록 조회", description = "키워드·활동 유형·날짜 범위 필터로 시스템 활동 로그를 페이징 조회합니다.", security = @SecurityRequirement(name = "jwtAuth"))
    @GetMapping
    public ResponseEntity<ApiResponse<Page<ActivityLogListDto>>>getActivityLogs(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) ActivityType activityType,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)LocalDateTime startDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
            ){
        return ResponseEntity.ok(ApiResponse.success(
                adminActivityLogService.getActivityLogs(
                        keyword,activityType,startDate,endDate, PageRequest.of(page,size))
        ));
    }
    @Operation(summary = "활동 로그 상세 조회", description = "특정 활동 로그의 요청 파라미터, 처리 결과 등 상세 정보를 반환합니다.", security = @SecurityRequirement(name = "jwtAuth"))
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ActivityLogDetailDto>> getActivityLogDetail(
            @PathVariable Long id
    ){
        return ResponseEntity.ok(ApiResponse.success(
                adminActivityLogService.getActivityLogDetail(id)
        ));
    }
}
