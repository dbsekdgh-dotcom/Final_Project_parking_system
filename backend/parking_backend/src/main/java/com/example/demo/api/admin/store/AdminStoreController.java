package com.example.demo.api.admin.store;

import com.example.demo.domain.management.store.dtos.request.StoreUpdateRequestDto;
import com.example.demo.domain.management.store.dtos.response.StoreDetailResponseDto;
import com.example.demo.domain.management.store.dtos.response.StoreListResponseDto;
import com.example.demo.domain.management.store.service.AdminStoreService;
import com.example.demo.domain.system.store.enums.Status;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/stores")
@RequiredArgsConstructor
public class AdminStoreController {
    private final AdminStoreService adminStoreService;

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
    @GetMapping("/{storeId}")
    public ResponseEntity<StoreDetailResponseDto> getStore(@PathVariable Long storeId){
        return ResponseEntity.ok(adminStoreService.getStore(storeId));
    }
    @PatchMapping("/{storeId}")
    public ResponseEntity<Void> updateStore(
            @PathVariable Long storeId,
            @RequestBody StoreUpdateRequestDto dto
            ){
        adminStoreService.updateStore(storeId,dto);
        return ResponseEntity.ok().build();
    }
    @PostMapping("/{storeId}/activate")
    public ResponseEntity<Void> activate(@PathVariable Long storeId){
        adminStoreService.activate(storeId);
        return ResponseEntity.ok().build();
    }
    @PostMapping("/{storeId}/deactivate")
    public ResponseEntity<Void> deactivate(@PathVariable Long storeId){
        adminStoreService.deactivate(storeId);
        return ResponseEntity.ok().build();
    }
}
