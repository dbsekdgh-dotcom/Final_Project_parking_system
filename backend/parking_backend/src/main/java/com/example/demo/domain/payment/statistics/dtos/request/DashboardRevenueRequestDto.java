package com.example.demo.domain.payment.statistics.dtos.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Setter@Getter
public class DashboardRevenueRequestDto {
    private String type;
    private int page;
    private int size;
}
