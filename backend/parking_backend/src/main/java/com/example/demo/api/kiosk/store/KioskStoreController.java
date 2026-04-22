package com.example.demo.api.kiosk.store;

import com.example.demo.domain.system.store.dtos.request.StoreLoginRequestDto;
import com.example.demo.domain.system.store.dtos.request.TicketApplyRequestDto;
import com.example.demo.domain.system.store.dtos.response.*;
import com.example.demo.domain.system.store.service.KioskStoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/store")
@RequiredArgsConstructor
public class KioskStoreController {

    private final KioskStoreService kioskStoreService;

    @PostMapping("/login")
    public ResponseEntity<StoreLoginResponseDto> login(@RequestBody StoreLoginRequestDto dto){
        return ResponseEntity.ok(kioskStoreService.login(dto));
    }
    @GetMapping("/me")
    public ResponseEntity<StoreInfoResponseDto> getMyInfo(){
        return ResponseEntity.ok(kioskStoreService.getMyInfo());
    }
    @GetMapping("/wallets")
    public ResponseEntity<List<StoreWalletResponseDto>> getWallets(){
        return ResponseEntity.ok(kioskStoreService.getWallets());
    }
    @GetMapping("/tickets/policies")
    public ResponseEntity<List<TicketPolicyResponseDto>> getPolicies(){
        return ResponseEntity.ok(kioskStoreService.getStorePolicies());
    }
    @GetMapping("/tickets/purchase/ready")
    public ResponseEntity<StorePurchaseReadyResponseDto> purchaseReady(
            @RequestParam Long ticketPolicyId,
            @RequestParam int quantity){
        return ResponseEntity.ok(kioskStoreService.purchaseReady(ticketPolicyId, quantity));
    }
    @PostMapping("/tickets/purchase/confirm")
    public ResponseEntity<Void> purchaseConfirm(
            @RequestParam Long ticketPolicyId,
            @RequestParam int quantity){
        kioskStoreService.purchaseConfirm(ticketPolicyId,quantity);
        return ResponseEntity.ok().build();
    }
    @GetMapping("/search-car")
    public ResponseEntity<List<StoreCarSearchResponseDto>> searchCar(
            @RequestParam String query){
        return ResponseEntity.ok(kioskStoreService.searchCar(query));
    }
    @PostMapping("/tickets/apply")
    public ResponseEntity<Void> applyTicket(@RequestBody TicketApplyRequestDto dto){
        kioskStoreService.applyTicket(dto);
        return ResponseEntity.ok().build();
    }
}
