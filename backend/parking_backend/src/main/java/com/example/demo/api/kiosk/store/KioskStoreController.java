package com.example.demo.api.kiosk.store;

import com.example.demo.domain.system.store.dtos.request.StoreLoginRequestDto;
import com.example.demo.domain.system.store.dtos.request.TicketApplyRequestDto;
import com.example.demo.domain.system.store.dtos.response.*;
import com.example.demo.domain.system.store.service.KioskStoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "5. 상가 (Store)", description = "상가 로그인, 내 정보 조회, 지갑 잔량, 할인권 구매·적용 API")
@RestController
@RequestMapping("/api/store")
@RequiredArgsConstructor
public class KioskStoreController {

    private final KioskStoreService kioskStoreService;

    @Operation(summary = "테스트용 상가 비밀번호 조회", description = "지정한 storeId의 상가명과 비밀번호를 반환합니다. 테스트 전용 공개 API입니다.")
    @GetMapping("/test-hint")
    public ResponseEntity<Map<String, String>> getTestHint(@RequestParam Long storeId) {
        return ResponseEntity.ok(kioskStoreService.getTestHint(storeId));
    }

    @Operation(summary = "상가 로그인", description = "상가 단말기 비밀번호로 로그인하고 JWT 토큰을 발급합니다.")
    @PostMapping("/login")
    public ResponseEntity<StoreLoginResponseDto> login(@RequestBody StoreLoginRequestDto dto){
        return ResponseEntity.ok(kioskStoreService.login(dto));
    }
    @Operation(summary = "상가 내 정보 조회", description = "로그인된 상가의 이름, 위치 등 기본 정보를 반환합니다.")
    @GetMapping("/me")
    public ResponseEntity<StoreInfoResponseDto> getMyInfo(){
        return ResponseEntity.ok(kioskStoreService.getMyInfo());
    }
    @Operation(summary = "상가 지갑 잔량 조회", description = "상가의 할인권 정책별 지갑 잔량(remainingCount > 0)을 반환합니다.")
    @GetMapping("/wallets")
    public ResponseEntity<List<StoreWalletResponseDto>> getWallets(){
        return ResponseEntity.ok(kioskStoreService.getWallets());
    }
    @Operation(summary = "구매 가능 할인권 정책 목록", description = "해당 상가가 구매 가능한 활성 할인권 정책 목록을 반환합니다.")
    @GetMapping("/tickets/policies")
    public ResponseEntity<List<TicketPolicyResponseDto>> getPolicies(){
        return ResponseEntity.ok(kioskStoreService.getStorePolicies());
    }
    @Operation(summary = "할인권 구매 준비", description = "ticketPolicyId·수량으로 구매 예상 금액을 계산하고 구매 준비 정보를 반환합니다.")
    @GetMapping("/tickets/purchase/ready")
    public ResponseEntity<StorePurchaseReadyResponseDto> purchaseReady(
            @RequestParam Long ticketPolicyId,
            @RequestParam int quantity){
        return ResponseEntity.ok(kioskStoreService.purchaseReady(ticketPolicyId, quantity));
    }
    @Operation(summary = "할인권 구매 확정", description = "결제 후 지갑 잔량을 증가시키고 구매 트랜잭션을 기록합니다.")
    @PostMapping("/tickets/purchase/confirm")
    public ResponseEntity<Void> purchaseConfirm(
            @RequestParam Long ticketPolicyId,
            @RequestParam int quantity){
        kioskStoreService.purchaseConfirm(ticketPolicyId,quantity);
        return ResponseEntity.ok().build();
    }
    @Operation(summary = "할인권 적용 차량 검색", description = "차량번호 일부(query)로 현재 주차 중인 차량을 검색합니다. 할인권 적용 대상 선택에 사용됩니다.")
    @GetMapping("/search-car")
    public ResponseEntity<List<StoreCarSearchResponseDto>> searchCar(
            @RequestParam String query){
        return ResponseEntity.ok(kioskStoreService.searchCar(query));
    }
    @Operation(summary = "할인권 적용", description = "선택한 차량의 주차 로그에 상가 할인권을 적용하고 지갑 잔량을 차감합니다.")
    @PostMapping("/tickets/apply")
    public ResponseEntity<Void> applyTicket(@RequestBody TicketApplyRequestDto dto){
        kioskStoreService.applyTicket(dto);
        return ResponseEntity.ok().build();
    }
}
