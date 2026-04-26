package com.example.demo.domain.dashboard.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MonthlyCountDto {
    private String month;
    private long count;
}
