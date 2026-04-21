package com.example.demo.domain.parking.space.dtos.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ParkingSpaceSummaryResponse {
    private long totalSpaces; //전체 구획(60)
    private long occupiedSpaces; //점유(현재주차중)
    private long availableSpaces; //가용(주차가능)
    private double occupancyRate; //점유율(%)

    private long b1Total;
    private long b2Total;
    private long b1Occupied;
    private long b2Occupied;
    private long b1Blocked;
    private long b2Blocked;
}
