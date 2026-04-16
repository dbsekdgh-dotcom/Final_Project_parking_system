package com.example.demo.domain.admin.management.fee.dtos.response;

import com.example.demo.domain.shared.ticketPolicy.TicketPolicy;
import com.example.demo.domain.shared.ticketPolicy.enums.DiscountType;
import com.example.demo.domain.shared.ticketPolicy.enums.Status;
import com.example.demo.domain.shared.ticketPolicy.enums.UseType;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@AllArgsConstructor
@Builder
@Getter
@Setter
public class TicketPolicyResponseDto {
    private long ticketPolicyId;
    private String name;
    private String description;
    private int price;
    private DiscountType discountType;
    private int discountValue;
    private UseType useType;
    private int maxDiscountAmount;
    private int validDays;
    private int validMinutes;
    private boolean stackable;
    private Status status;
    private boolean isFreeTicket;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    public static TicketPolicyResponseDto toTicketPolicyDto(TicketPolicy ticketPolicy){
        return TicketPolicyResponseDto.builder()
                .ticketPolicyId(ticketPolicy.getTicketPolicyId())
                .name(ticketPolicy.getName())
                .description(ticketPolicy.getDescription())
                .price(ticketPolicy.getPrice())
                .discountType(ticketPolicy.getDiscountType())
                .discountValue(ticketPolicy.getDiscountValue())
                .useType(ticketPolicy.getUseType())
                .maxDiscountAmount(ticketPolicy.getMaxDiscountAmount())
                .validDays(ticketPolicy.getValidDays())
                .validMinutes(ticketPolicy.getValidMinutes())
                .stackable(ticketPolicy.isStackable())
                .status(ticketPolicy.getStatus())
                .isFreeTicket(ticketPolicy.isFreeTicket())
                .createdAt(ticketPolicy.getCreatedAt())
                .build();
    }
}
