package com.example.demo.domain.payment.statistics.dtos.response;

import lombok.*;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class DailyRevenueDto {
    private LocalDate date;
    private long payAmount;
    private long pointAmount;
    private long ticketUsedAmount;
    private long totalAmount;
}
