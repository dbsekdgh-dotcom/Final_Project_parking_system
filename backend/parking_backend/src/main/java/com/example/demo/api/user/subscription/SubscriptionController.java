package com.example.demo.api.user.subscription;

import com.example.demo.domain.auth.user.principal.PrincipalDetails;
import com.example.demo.domain.payment.subscription.dtos.request.SubscriptionConfirmRequestDto;
import com.example.demo.domain.payment.subscription.dtos.request.SubscriptionReadyRequestDto;
import com.example.demo.domain.payment.subscription.dtos.response.SubscriptionHistoryResponseDto;
import com.example.demo.domain.payment.subscription.dtos.response.SubscriptionPolicyResponseDto;
import com.example.demo.domain.payment.subscription.dtos.response.SubscriptionMyInfoResponseDto;
import com.example.demo.domain.payment.subscription.dtos.response.SubscriptionReadyResponseDto;
import com.example.demo.domain.payment.subscription.dtos.response.SubscriptionRefundResponseDto;
import com.example.demo.domain.payment.subscription.service.SubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@Tag(name = "7. 정기권 (Subscription)", description = "정기권 결제 준비 및 확정 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user/subscriptions")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @Operation(summary = "정기권 정책 조회", description = "가격, 이용 기간, 선택한 시작일 기준 남은 슬롯 수를 반환합니다.", security = @SecurityRequirement(name = "jwtAuth"))
    @GetMapping("/policy")
    public ResponseEntity<SubscriptionPolicyResponseDto> getSubscriptionPolicy(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate) {

        return ResponseEntity.ok(subscriptionService.getSubscriptionPolicy(startDate));
    }

    @Operation(summary = "정기권 구매 이력 조회", description = "내가 구매한 모든 정기권 이력을 최신순으로 반환합니다.", security = @SecurityRequirement(name = "jwtAuth"))
    @GetMapping("/history")
    public ResponseEntity<List<SubscriptionHistoryResponseDto>> getMySubscriptionHistory(
            @AuthenticationPrincipal PrincipalDetails principalDetails) {

        return ResponseEntity.ok(
                subscriptionService.getMySubscriptionHistory(principalDetails.getUserId())
        );
    }

    @Operation(summary = "내 정기권 조회", description = "현재 ACTIVE 상태인 정기권과 남은 일수를 반환합니다. 정기권이 없으면 null을 반환합니다.", security = @SecurityRequirement(name = "jwtAuth"))
    @GetMapping("/my")
    public ResponseEntity<SubscriptionMyInfoResponseDto> getMySubscription(
            @AuthenticationPrincipal PrincipalDetails principalDetails) {

        return ResponseEntity.ok(
                subscriptionService.getMySubscription(principalDetails.getUserId())
        );
    }

    @Operation(summary = "정기권 결제 준비", description = "시작일과 차량번호를 입력해 정기권 결제를 준비합니다. 선착순 슬롯 및 중복 여부를 검증하고 orderId를 반환합니다.", security = @SecurityRequirement(name = "jwtAuth"))
    @PostMapping("/ready")
    public ResponseEntity<SubscriptionReadyResponseDto> prepareSubscription(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @Valid @RequestBody SubscriptionReadyRequestDto requestDto) {

        return ResponseEntity.ok(
                subscriptionService.prepareSubscription(principalDetails.getUserId(), requestDto)
        );
    }

    @Operation(summary = "정기권 결제 확정", description = "토스페이먼츠 승인 결과를 받아 정기권을 최종 발급합니다.", security = @SecurityRequirement(name = "jwtAuth"))
    @PostMapping("/confirm")
    public ResponseEntity<Void> confirmSubscription(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @Valid @RequestBody SubscriptionConfirmRequestDto requestDto) {

        subscriptionService.confirmSubscription(principalDetails.getUserId(), requestDto);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "정기권 환불", description = "시작 전이면 전액, 시작 후면 남은 일수 기준 일할 계산하여 부분 환불합니다.", security = @SecurityRequirement(name = "jwtAuth"))
    @PostMapping("/{subscriptionId}/refund")
    public ResponseEntity<SubscriptionRefundResponseDto> refundSubscription(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @PathVariable Long subscriptionId) {

        return ResponseEntity.ok(
                subscriptionService.refundSubscription(principalDetails.getUserId(), subscriptionId)
        );
    }
}
