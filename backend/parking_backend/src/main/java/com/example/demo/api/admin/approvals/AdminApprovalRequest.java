package com.example.demo.api.admin.approvals;

import com.example.demo.domain.approval.dtos.request.ApprovalRejectRequestDto;
import com.example.demo.domain.approval.dtos.response.AppprovalPageResponseDto;
import com.example.demo.domain.approval.enums.ApprovalStatus;
import com.example.demo.domain.approval.enums.ApprovalType;
import com.example.demo.domain.approval.service.AdminApprovalService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/approvals")
@RequiredArgsConstructor
public class AdminApprovalRequest {
    private final AdminApprovalService adminApprovalService;

    @GetMapping
    public ResponseEntity<AppprovalPageResponseDto> getApprovals(
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
    @PostMapping("/{approvalId}/approve")
    public ResponseEntity<Void> approve(@PathVariable Long approvalId){
        adminApprovalService.approve(approvalId);
        return ResponseEntity.ok().build();
    }
    @PostMapping("/{approvalId}/reject")
    public ResponseEntity<Void> reject(@PathVariable Long approvalId,
                                       @RequestBody ApprovalRejectRequestDto dto){
        adminApprovalService.reject(approvalId, dto.getRejectReason());
        return ResponseEntity.ok().build();
    }
}
