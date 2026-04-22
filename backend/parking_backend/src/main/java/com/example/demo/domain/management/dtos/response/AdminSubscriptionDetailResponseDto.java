package com.example.demo.domain.management.dtos.response;

import com.example.demo.domain.payment.Payment;
import com.example.demo.domain.payment.subscription.Subscription;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AdminSubscriptionDetailResponseDto {
    private Long subscriptionId;
    private String ownerName;
    private String carNumber;
    private String vehicleName;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String status;
    private Integer price;
    private LocalDateTime createdAt;
    private LocalDateTime activatedAt;
    private LocalDateTime cancelledAt;

    private Long paymentAmount;
    private String paymentMethod;
    private String paymentStatus;
    private LocalDateTime paidAt;
    private Long refundedAmount;

    public static AdminSubscriptionDetailResponseDto from(Subscription s){
        Payment p = s.getPayment();
        return AdminSubscriptionDetailResponseDto.builder()
                .subscriptionId(s.getSubscriptionId())
                .ownerName(s.getUser().getName())
                .carNumber(s.getVehicle().getCarNumber())
                .vehicleName(s.getVehicle().getVehicleName())
                .startDate(s.getStartDate())
                .endDate(s.getEndDate())
                .status(s.getStatus().name())
                .price(s.getPrice())
                .createdAt(s.getCreatedAt())
                .activatedAt(s.getActivatedAt())
                .cancelledAt(s.getCancelledAt())
                .paymentAmount(p != null ? p.getAmount() : null)
                .paymentMethod(p != null ? p.getPaymentMethod().name() : null)
                .paymentStatus(p != null ? p.getPaymentStatus().name() : null)
                .paidAt(p != null ? p.getPaidAt() : null)
                .refundedAmount( p != null ? p.getRefundedAmount() : null)
                .build();
    }
}
