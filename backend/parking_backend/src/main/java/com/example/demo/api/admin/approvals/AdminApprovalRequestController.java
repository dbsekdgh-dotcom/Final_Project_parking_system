package com.example.demo.api.admin.approvals;

import com.example.demo.domain.approval.dtos.request.ApprovalRejectRequestDto;
import com.example.demo.domain.approval.dtos.response.ApprovalPageResponseDto;
import com.example.demo.domain.approval.enums.ApprovalStatus;
import com.example.demo.domain.approval.enums.ApprovalType;
import com.example.demo.domain.approval.service.AdminApprovalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "8. 승인 관리 (Approval)", description = "차량 등록 등 사용자 승인 요청 목록 조회, 승인, 거절 API")
@RestController
@RequestMapping("/api/admin/approvals")
@RequiredArgsConstructor
public class AdminApprovalRequestController {
    private final AdminApprovalService adminApprovalService;

    @Operation(summary = "승인 요청 목록 조회", description = "타입(차량 등록 등)·상태·키워드 필터로 승인 요청 목록을 페이징 조회합니다.", security = @SecurityRequirement(name = "jwtAuth"))
    @GetMapping
    public ResponseEntity<ApprovalPageResponseDto> getApprovals(
            @RequestParam(required = false) ApprovalType type,
            @RequestParam(required = false) ApprovalStatus status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
            ){
        return ResponseEntity.ok(
                adminApprovalService.getApprovals(type, status, keyword, PageRequest.of(page, size))
        );
    }
    @Operation(summary = "승인 처리", description = "지정한 승인 요청을 승인합니다.", security = @SecurityRequirement(name = "jwtAuth"))
    @PostMapping("/{approvalId}/approve")
    public ResponseEntity<Void> approve(@PathVariable Long approvalId){
        adminApprovalService.approve(approvalId);
        return ResponseEntity.ok().build();
    }
    @Operation(summary = "승인 거절", description = "지정한 승인 요청을 거절합니다. 거절 사유를 body에 포함해야 합니다.", security = @SecurityRequirement(name = "jwtAuth"))
    @PostMapping("/{approvalId}/reject")
    public ResponseEntity<Void> reject(@PathVariable Long approvalId,
                                       @RequestBody ApprovalRejectRequestDto dto){
        adminApprovalService.reject(approvalId, dto.getRejectReason());
        return ResponseEntity.ok().build();
    }
}
