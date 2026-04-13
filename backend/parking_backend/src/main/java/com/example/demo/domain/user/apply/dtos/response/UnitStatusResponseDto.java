package com.example.demo.domain.user.apply.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UnitStatusResponseDto {

    private Long householdId;
    private Integer unitNo;
    private String status;
}
