package com.example.demo.domain.parking.policy.dtos.response;

import com.example.demo.domain.payment.ticketpolicy.TicketPolicy;
import com.example.demo.domain.payment.ticketpolicy.enums.DiscountType;
import com.example.demo.domain.payment.ticketpolicy.enums.Status;
import com.example.demo.domain.payment.ticketpolicy.enums.UseType;
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
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    public static TicketPolicyResponseDto toTicketPolicyDto(TicketPolicy ticketPolicy){
        return TicketPolicyResponseDto.builder()
                .ticketPolicyId(ticketPolicy.getTicketPolicyId())
                .name(ticketPolicy.getName())
                .description(ticketPolicy.getDescription()!=null?ticketPolicy.getDescription():"")
                .price(ticketPolicy.getPrice())
                .discountType(ticketPolicy.getDiscountType())
                .discountValue(ticketPolicy.getDiscountValue())
                .useType(ticketPolicy.getUseType())
                .maxDiscountAmount(ticketPolicy.getMaxDiscountAmount()!=null?ticketPolicy.getMaxDiscountAmount():0)
                .validDays(ticketPolicy.getValidDays()!=null?ticketPolicy.getValidDays():0)
                .validMinutes(ticketPolicy.getValidMinutes()!=null?ticketPolicy.getValidMinutes():0)
                .stackable(ticketPolicy.isStackable())
                .status(ticketPolicy.getStatus())
                .isFreeTicket(ticketPolicy.isFreeTicket())
                .createdAt(ticketPolicy.getCreatedAt())
                .build();
    }
}
