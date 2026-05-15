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

    @Operation(summary = "블랙리스트 목록 조회", description = "키워드·상태·신고 유형 필터로 블랙리스트 차량 목록을 페이징 조회합니다.", security = @SecurityRequirement(name = "jwtAuth"))
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

    @Operation(summary = "블랙리스트 상세 조회", description = "특정 블랙리스트 항목의 차량번호, 신고 유형, 등록 사유, 상태 등을 반환합니다.", security = @SecurityRequirement(name = "jwtAuth"))
    @GetMapping("/{id}")
    public ResponseEntity<AdminBlacklistDetailResponseDto> getDetail(@PathVariable Long id) {
        return ResponseEntity.ok(adminBlacklistService.getDetail(id));
    }

    @Operation(summary = "블랙리스트 등록", description = "차량번호와 신고 유형·사유를 입력하여 블랙리스트에 등록합니다.", security = @SecurityRequirement(name = "jwtAuth"))
    @PostMapping
    public ResponseEntity<Void> register(@RequestBody @Valid AdminBlacklistRequestDto dto) {
        adminBlacklistService.register(dto);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "블랙리스트 해제", description = "지정한 블랙리스트 항목을 해제하여 해당 차량의 입차를 다시 허용합니다.", security = @SecurityRequirement(name = "jwtAuth"))
    @PatchMapping("/{id}/release")
    public ResponseEntity<Void> release(@PathVariable Long id) {
        adminBlacklistService.release(id);
        return ResponseEntity.ok().build();
    }
}
