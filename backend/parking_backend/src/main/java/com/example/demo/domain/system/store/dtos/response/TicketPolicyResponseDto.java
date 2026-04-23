package com.example.demo.domain.system.store.dtos.response;

import com.example.demo.domain.payment.ticketpolicy.TicketPolicy;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TicketPolicyResponseDto {
    private Long ticketPolicyId;
    private String name;
    private String description;
    private Integer price;
    private String discountType;
    private Integer discountValue;
    private boolean stackable;

    public static TicketPolicyResponseDto from(TicketPolicy policy){
        return TicketPolicyResponseDto.builder()
                .ticketPolicyId(policy.getTicketPolicyId())
                .name(policy.getName())
                .description(policy.getDescription())
                .price(policy.getPrice())
                .discountType(policy.getDiscountType().name())
                .discountValue(policy.getDiscountValue())
                .stackable(policy.isStackable())
                .build();
    }
}
