package com.example.demo.domain.kiosk.payment.dtos.internal;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AppliedTicketResult {
    private long ticketId;
    private long policyId;
    private long storeId;
    private int appliedValue;
}
