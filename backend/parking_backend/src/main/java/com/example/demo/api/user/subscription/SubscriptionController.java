package com.example.demo.api.user.subscription;

import com.example.demo.domain.auth.user.principal.PrincipalDetails;
import com.example.demo.domain.payment.subscription.dtos.request.SubscriptionPurchaseRequestDto;
import com.example.demo.domain.payment.subscription.dtos.response.SubscriptionPolicyResponseDto;
import com.example.demo.domain.payment.subscription.dtos.response.SubscriptionRefundResponseDto;
import com.example.demo.domain.payment.subscription.dtos.response.SubscriptionResponseDto;
import com.example.demo.domain.payment.subscription.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user/subscriptions")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @GetMapping("/point")
    public ResponseEntity<Integer> getMyPoint(
            @AuthenticationPrincipal PrincipalDetails principal) {
        return ResponseEntity.ok(subscriptionService.getMyPoint(principal.getUserId()));
    }

    @GetMapping("/policy")
    public ResponseEntity<SubscriptionPolicyResponseDto> getPolicy(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate) {
        return ResponseEntity.ok(subscriptionService.getPolicy(startDate));
    }

    @GetMapping("/my")
    public ResponseEntity<List<SubscriptionResponseDto>> getMySubscriptions(
            @AuthenticationPrincipal PrincipalDetails principal) {
        return ResponseEntity.ok(subscriptionService.getMySubscriptions(principal.getUserId()));
    }

    @PostMapping
    public ResponseEntity<SubscriptionResponseDto> purchase(
            @AuthenticationPrincipal PrincipalDetails principal,
            @RequestBody SubscriptionPurchaseRequestDto dto) {
        return ResponseEntity.ok(subscriptionService.purchase(principal.getUserId(), dto));
    }

    @PostMapping("/{subscriptionId}/cancel")
    public ResponseEntity<SubscriptionRefundResponseDto> cancel(
            @AuthenticationPrincipal PrincipalDetails principal,
            @PathVariable Long subscriptionId) {
        return ResponseEntity.ok(subscriptionService.cancel(principal.getUserId(), subscriptionId));
    }
}
