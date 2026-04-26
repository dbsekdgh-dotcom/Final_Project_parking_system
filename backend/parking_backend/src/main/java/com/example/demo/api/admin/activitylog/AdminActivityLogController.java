package com.example.demo.api.admin.activitylog;

import com.example.demo.domain.system.activitylog.dtos.response.ActivityLogDetailDto;
import com.example.demo.domain.system.activitylog.dtos.response.ActivityLogListDto;
import com.example.demo.domain.system.activitylog.enums.ActivityType;
import com.example.demo.domain.system.activitylog.service.AdminActivityLogService;
import com.example.demo.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/admin/activity-logs")
@RequiredArgsConstructor
public class AdminActivityLogController {

    private final AdminActivityLogService adminActivityLogService;

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
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ActivityLogDetailDto>> getActivityLogDetail(
            @PathVariable Long id
    ){
        return ResponseEntity.ok(ApiResponse.success(
                adminActivityLogService.getActivityLogDetail(id)
        ));
    }
}
