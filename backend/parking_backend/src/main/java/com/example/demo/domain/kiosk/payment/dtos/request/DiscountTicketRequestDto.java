package com.example.demo.domain.kiosk.payment.dtos.request;

import com.example.demo.domain.shared.ticketPolicy.TicketPolicy;
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
