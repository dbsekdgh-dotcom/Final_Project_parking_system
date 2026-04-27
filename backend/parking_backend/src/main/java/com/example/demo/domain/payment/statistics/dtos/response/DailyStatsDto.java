package com.example.demo.domain.payment.statistics.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class DailyStatsDto {
    private LocalDate date;
    private long parkingRevenue;
    private long ticketRevenue;
    private long subscriptionRevenue;
    private long refund;
    private long commission;
    private long pointDiscount;

    public long getNet(){
        return parkingRevenue+ticketRevenue+subscriptionRevenue-refund-commission-pointDiscount;
    }
}
