package com.example.demo.api.user.subscription;

import com.example.demo.domain.auth.user.principal.PrincipalDetails;
import com.example.demo.domain.payment.subscription.dtos.request.SubscriptionPurchaseRequestDto;
import com.example.demo.domain.payment.subscription.dtos.response.SubscriptionPolicyResponseDto;
import com.example.demo.domain.payment.subscription.dtos.response.SubscriptionRefundResponseDto;
import com.example.demo.domain.payment.subscription.dtos.response.SubscriptionResponseDto;
import com.example.demo.domain.payment.subscription.service.SubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "7. 정기권 (Subscription)", description = "정기권 구매, 조회, 환불 및 정책·포인트 조회 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user/subscriptions")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @Operation(summary = "내 포인트 조회", description = "현재 로그인한 사용자의 보유 포인트를 반환합니다.")
    @SecurityRequirement(name = "jwtAuth")
    @GetMapping("/point")
    public ResponseEntity<Integer> getMyPoint(
            @AuthenticationPrincipal PrincipalDetails principal) {
        return ResponseEntity.ok(subscriptionService.getMyPoint(principal.getUserId()));
    }

    @Operation(summary = "정기권 정책 조회", description = "정기권 가격, 적용 기간 등 현재 단지 정기권 정책을 반환합니다. startDate를 넘기면 해당 시점 기준 정책을 계산합니다.")
    @SecurityRequirement(name = "jwtAuth")
    @GetMapping("/policy")
    public ResponseEntity<SubscriptionPolicyResponseDto> getPolicy(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate) {
        return ResponseEntity.ok(subscriptionService.getPolicy(startDate));
    }

    @Operation(summary = "내 정기권 목록 조회", description = "현재 로그인한 사용자가 구매한 정기권 전체 이력을 반환합니다.")
    @SecurityRequirement(name = "jwtAuth")
    @GetMapping("/my")
    public ResponseEntity<List<SubscriptionResponseDto>> getMySubscriptions(
            @AuthenticationPrincipal PrincipalDetails principal) {
        return ResponseEntity.ok(subscriptionService.getMySubscriptions(principal.getUserId()));
    }

    @Operation(summary = "정기권 구매", description = "포인트를 사용해 정기권을 구매합니다. 보유 포인트가 부족하거나 이미 유효한 정기권이 있는 경우 구매가 제한됩니다.")
    @SecurityRequirement(name = "jwtAuth")
    @PostMapping
    public ResponseEntity<SubscriptionResponseDto> purchase(
            @AuthenticationPrincipal PrincipalDetails principal,
            @RequestBody SubscriptionPurchaseRequestDto dto) {
        return ResponseEntity.ok(subscriptionService.purchase(principal.getUserId(), dto));
    }

    @Operation(summary = "정기권 환불", description = "구매한 정기권을 취소하고 포인트를 환불합니다. 이미 사용 중(입차 기록 있음)이거나 만료된 정기권은 환불이 불가합니다.")
    @SecurityRequirement(name = "jwtAuth")
    @PostMapping("/{subscriptionId}/cancel")
    public ResponseEntity<SubscriptionRefundResponseDto> cancel(
            @AuthenticationPrincipal PrincipalDetails principal,
            @PathVariable Long subscriptionId) {
        return ResponseEntity.ok(subscriptionService.cancel(principal.getUserId(), subscriptionId));
    }
}
