package com.example.demo.domain.kiosk.payment.dtos.request;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Builder
@Getter
public class RefundRequestDto {
    private String paymentKey;          // 결제 번호
    private String vehicleNumber;    // 차량 번호
    private long totalRefundAmount;  // 총 환불 금액
    private long creditRefundAmount; // 카드 환불액
    private int pointRestoreAmount;  // 포인트 복구액
    private String refundStatus;     // 환불 상태 (SUCCESS 등)
    private LocalDateTime refundedAt; // 환불 완료 시점
}
