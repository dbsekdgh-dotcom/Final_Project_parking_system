package com.example.demo.api.admin.actionlog;

import com.example.demo.domain.auth.admin.dtos.response.ActionLogResponseDto;
import com.example.demo.domain.auth.admin.enums.ActionType;
import com.example.demo.domain.auth.admin.enums.TargetType;
import com.example.demo.domain.auth.admin.service.AdminActionLogQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/admin/action-logs")
@RequiredArgsConstructor
public class AdminActionLogController {
    private final AdminActionLogQueryService adminActionLogQueryService;

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
    @PostMapping("/{actionId}/revert")
    public ResponseEntity<Void> revert(@PathVariable Long actionId){
        adminActionLogQueryService.revert(actionId);
        return ResponseEntity.ok().build();
    }
}
