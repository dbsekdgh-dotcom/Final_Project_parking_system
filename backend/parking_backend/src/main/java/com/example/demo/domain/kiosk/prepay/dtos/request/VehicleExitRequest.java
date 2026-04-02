package com.example.demo.domain.kiosk.prepay.dtos.request;

import com.example.demo.domain.shared.parkinglog.enums.ParkingStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class VehicleExitRequest {
    private Long parkingLogId;
    private String vehicleNumber;
    private LocalDateTime exiteTime;
    private ParkingStatus parkingStatus;
    private Integer rawFee;
    private Integer totalDiscountMinutes;
    private Integer totalDiscountAmount;
    private Long calculatedFee;
}
