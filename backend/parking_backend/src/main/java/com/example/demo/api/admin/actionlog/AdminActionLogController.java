package com.example.demo.api.admin.actionlog;

import com.example.demo.domain.auth.admin.dtos.response.ActionLogResponseDto;
import com.example.demo.domain.auth.admin.enums.ActionType;
import com.example.demo.domain.auth.admin.enums.TargetType;
import com.example.demo.domain.auth.admin.service.AdminActionLogQueryService;
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

@Tag(name = "15. 액션 로그 (Action Log)", description = "관리자 액션 로그 목록 조회 및 이전 상태 되돌리기 API")
@RestController
@RequestMapping("/api/admin/action-logs")
@RequiredArgsConstructor
public class AdminActionLogController {
    private final AdminActionLogQueryService adminActionLogQueryService;

    @Operation(summary = "액션 로그 목록 조회", description = "대상 타입·액션 타입·되돌리기 여부·키워드·날짜 범위 필터로 관리자 액션 이력을 페이징 조회합니다.", security = @SecurityRequirement(name = "jwtAuth"))
    @GetMapping
    public ResponseEntity<Page<ActionLogResponseDto>> getActionLogs(
            @RequestParam(required = false) TargetType targetType,
            @RequestParam(required = false) ActionType actionType,
            @RequestParam(required = false) Boolean isReverted,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
            ){
        return ResponseEntity.ok(
                adminActionLogQueryService.getActionLogs(
                        targetType,actionType,isReverted,keyword,startDate,endDate,PageRequest.of(page,size)
                )
        );
    }
    @Operation(summary = "액션 되돌리기", description = "UPDATE 타입 액션 로그를 이전 상태로 되돌립니다. POLICY·PARKING_SPACE·PARKING_LOG·STORE 대상만 지원합니다.", security = @SecurityRequirement(name = "jwtAuth"))
    @PostMapping("/{actionId}/revert")
    public ResponseEntity<Void> revert(@PathVariable Long actionId){
        adminActionLogQueryService.revert(actionId);
        return ResponseEntity.ok().build();
    }
}
