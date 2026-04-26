package com.example.demo.domain.payment.statistics.dtos.internal;

import lombok.*;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
public class ParkingLogDiscountValueDto {
    private LocalDate date;
    private long parkingLogId;
    private long ticketUsedAmount;
    private long payAmount;
    private long pointAmount;
    private long totalAmount;
}
