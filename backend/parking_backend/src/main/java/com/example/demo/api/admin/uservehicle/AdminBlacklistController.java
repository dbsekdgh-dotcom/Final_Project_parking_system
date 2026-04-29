package com.example.demo.api.admin.uservehicle;

import com.example.demo.domain.management.dtos.request.AdminBlacklistRequestDto;
import com.example.demo.domain.management.dtos.response.AdminBlacklistDetailResponseDto;
import com.example.demo.domain.management.dtos.response.AdminBlacklistResponseDto;
import com.example.demo.domain.management.service.AdminBlacklistService;
import com.example.demo.domain.vehicle.blacklist.enums.BlacklistReasonType;
import com.example.demo.domain.vehicle.blacklist.enums.BlacklistStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "17. 블랙리스트 관리 (Blacklist)", description = "차량 블랙리스트 등록·해제·목록·상세 API")
@RestController
@RequestMapping("/api/admin/management/blacklist")
@RequiredArgsConstructor
public class AdminBlacklistController {

    private final AdminBlacklistService adminBlacklistService;

    @Operation(summary = "블랙리스트 목록 조회", security = @SecurityRequirement(name = "jwtAuth"))
    @GetMapping
    public ResponseEntity<Page<AdminBlacklistResponseDto>> getBlacklist(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) BlacklistStatus status,
            @RequestParam(required = false) BlacklistReasonType reasonType,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(
                adminBlacklistService.getBlacklist(keyword, status, reasonType, PageRequest.of(page, size))
        );
    }

    @Operation(summary = "블랙리스트 상세 조회", security = @SecurityRequirement(name = "jwtAuth"))
    @GetMapping("/{id}")
    public ResponseEntity<AdminBlacklistDetailResponseDto> getDetail(@PathVariable Long id) {
        return ResponseEntity.ok(adminBlacklistService.getDetail(id));
    }

    @Operation(summary = "블랙리스트 등록", security = @SecurityRequirement(name = "jwtAuth"))
    @PostMapping
    public ResponseEntity<Void> register(@RequestBody @Valid AdminBlacklistRequestDto dto) {
        adminBlacklistService.register(dto);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "블랙리스트 해제", security = @SecurityRequirement(name = "jwtAuth"))
    @PatchMapping("/{id}/release")
    public ResponseEntity<Void> release(@PathVariable Long id) {
        adminBlacklistService.release(id);
        return ResponseEntity.ok().build();
    }
}
