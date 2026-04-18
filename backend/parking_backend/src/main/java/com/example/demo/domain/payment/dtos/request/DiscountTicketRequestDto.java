package com.example.demo.domain.payment.dtos.request;

import com.example.demo.domain.payment.ticketpolicy.TicketPolicy;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class DiscountTicketRequestDto {
    private Long parkingTicketId;
    private Long parkingLogId;
    private TicketPolicy ticketPolicy;
}
