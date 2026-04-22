package com.example.demo.domain.system.store.dtos.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TicketApplyRequestDto {
    private Long ticketPolicyId;
    private Long parkingLogId;
    private int quantity;
}
