package com.example.demo.domain.management.dtos.response;

import com.example.demo.domain.payment.subscription.Subscription;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AdminSubscriptionResponseDto {
    private Long subscriptionId;
    private String ownerName;
    private String carNumber;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String status;
    private Integer price;
    private LocalDateTime createdAt;

    public static AdminSubscriptionResponseDto from(Subscription s){
        return AdminSubscriptionResponseDto.builder()
                .subscriptionId(s.getSubscriptionId())
                .ownerName(s.getUser().getName())
                .carNumber(s.getVehicle().getCarNumber())
                .startDate(s.getStartDate())
                .endDate(s.getEndDate())
                .status(s.getStatus().name())
                .price(s.getPrice())
                .createdAt(s.getCreatedAt())
                .build();
    }
}
