package com.example.demo.api.admin.store;

import com.example.demo.domain.management.store.dtos.request.StoreActivateRequestDto;
import com.example.demo.domain.management.store.dtos.request.StoreTicketConfigRequestDto;
import com.example.demo.domain.management.store.dtos.request.StoreUpdateRequestDto;
import com.example.demo.domain.management.store.dtos.response.StoreDetailResponseDto;
import com.example.demo.domain.management.store.dtos.response.StoreListResponseDto;
import com.example.demo.domain.management.store.dtos.response.StoreTicketConfigResponseDto;
import com.example.demo.domain.management.store.service.AdminStoreService;
import com.example.demo.domain.system.store.enums.Status;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "13. 상가 관리 (Store)", description = "상가 목록/상세 조회, 정보 수정, 입주·퇴거 처리 API")
@RestController
@RequestMapping("/api/admin/stores")
@RequiredArgsConstructor
public class AdminStoreController {
    private final AdminStoreService adminStoreService;

    @Operation(summary = "상가 목록 조회", description = "키워드·상태(ACTIVE/INACTIVE) 필터로 상가 목록을 페이징 조회합니다.", security = @SecurityRequirement(name = "jwtAuth"))
    @GetMapping
    public ResponseEntity<Page<StoreListResponseDto>> getStores(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Status status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ){
        return ResponseEntity.ok(
                adminStoreService.getStores(keyword, status, PageRequest.of(page,size))
        );
    }
    @Operation(summary = "상가 상세 조회", description = "특정 상가의 이름, 위치, 상태, 지갑 잔량 등을 반환합니다.", security = @SecurityRequirement(name = "jwtAuth"))
    @GetMapping("/{storeId}")
    public ResponseEntity<StoreDetailResponseDto> getStore(@PathVariable Long storeId){
        return ResponseEntity.ok(adminStoreService.getStore(storeId));
    }
    @Operation(summary = "상가 정보 수정", description = "상가의 이름과 단말기 비밀번호를 수정합니다. 위치·생성일은 수정 불가합니다.", security = @SecurityRequirement(name = "jwtAuth"))
    @PatchMapping("/{storeId}")
    public ResponseEntity<Void> updateStore(
            @PathVariable Long storeId,
            @RequestBody StoreUpdateRequestDto dto
            ){
        adminStoreService.updateStore(storeId,dto);
        return ResponseEntity.ok().build();
    }
    @Operation(summary = "상가 입주 처리", description = "INACTIVE 상가를 ACTIVE로 전환합니다. 새 비밀번호 설정 및 deleted_at 초기화가 함께 처리됩니다.", security = @SecurityRequirement(name = "jwtAuth"))
    @PostMapping("/{storeId}/activate")
    public ResponseEntity<Void> activate(@PathVariable Long storeId,
                                         @RequestBody@Valid StoreActivateRequestDto dto){
        adminStoreService.activate(storeId, dto);
        return ResponseEntity.ok().build();
    }
    @Operation(summary = "상가 퇴거 처리", description = "ACTIVE 상가를 INACTIVE로 전환합니다. deleted_at 설정 및 지갑 잔량이 전부 0으로 초기화됩니다.", security = @SecurityRequirement(name = "jwtAuth"))
    @PostMapping("/{storeId}/deactivate")
    public ResponseEntity<Void> deactivate(@PathVariable Long storeId){
        adminStoreService.deactivate(storeId);
        return ResponseEntity.ok().build();
    }
    @Operation(summary = "할인권 월 무료 발급 설정 조회", description = "상가의 월 무료 발급 할인권 설정을 조회 합니다.", security = @SecurityRequirement(name = "jwtAuth"))
    @GetMapping("/{storeId}/ticket-config")
    public ResponseEntity<StoreTicketConfigResponseDto> getTicketConfig(@PathVariable Long storeId){
        return adminStoreService.getTicketConfig(storeId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }
    @Operation(summary = "할인권 월 무료 발급 설정 저장/수정", description = "상가의 월 무료 발급 할인권의 설정을 저장 및 수정 합니다.", security = @SecurityRequirement(name = "jwtAuth"))
    @PutMapping("/{storeId}/ticket-config")
    public ResponseEntity<Void> setTicketConfig(@PathVariable Long storeId,
                                                @RequestBody @Valid StoreTicketConfigRequestDto dto){
        adminStoreService.setTicketConfig(storeId,dto);
        return ResponseEntity.ok().build();
    }
    @Operation(summary = "무료 할인권 정책 목록", description = "관리자가 선택할 할인권 정책 목록 드롭다운에 출력할 정책 조회", security = @SecurityRequirement(name = "jwtAuth"))
    @GetMapping("/free-ticket-policies")
    public ResponseEntity<List<Map<String,Object>>> getFreeTicketPolicies(){
        List<Map<String ,Object>> list = adminStoreService.getFreeTicketPolicies().stream()
                .map(p -> Map.<String, Object>of(
                        "ticketPolicyId",p.getTicketPolicyId(),
                        "name", p.getName()))
                .toList();
        return ResponseEntity.ok(list);
    }
}
