package com.example.demo.domain.parking.space.dtos.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserParkingSummaryDto {
    private long totalSpaces;
    private long occupiedSpaces;
    private long availableSpaces;
    private double occupancyRate;
    private String targetFloor;
}
