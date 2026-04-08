package com.example.demo.domain.shared.parkinglog.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ParkingLogSettlementDto {
    private Long parkingLogId;
    private String vehicleNumber;
    private Long userId;
    private Integer userPoint;
}
