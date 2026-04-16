package com.example.demo.domain.admin.management.parkingSpace.dtos.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ParkingSpaceSummaryResponse {
    private long totalSpaces; //전체 구획(60)
    private long occupiedSpaces; //점유(현재주차중)
    private long availableSpaces; //가용(주차가능)
    private double occupancyRate; //점유율(%)

    private String b1Status;
    private String b2Status;
}
