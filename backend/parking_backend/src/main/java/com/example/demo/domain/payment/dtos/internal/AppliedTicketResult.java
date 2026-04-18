package com.example.demo.domain.payment.dtos.internal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString
public class AppliedTicketResult {
    private long ticketId;
    private long policyId;
    private long storeId;
    private int appliedValue;
}
