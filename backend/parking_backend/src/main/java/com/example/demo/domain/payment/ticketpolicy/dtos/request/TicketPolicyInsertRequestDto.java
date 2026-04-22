package com.example.demo.domain.payment.ticketpolicy.dtos.request;

import com.example.demo.domain.payment.ticketpolicy.enums.DiscountType;
import com.example.demo.domain.payment.ticketpolicy.enums.UseType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class TicketPolicyInsertRequestDto {
    private String name;
    private String description;
    private int price;
    private DiscountType discountType;
    private int discountValue;
    private UseType useType;
    private int validMinutes;
    private int validDays;
    private boolean stackable;
    private boolean isFreeTicket;
}
