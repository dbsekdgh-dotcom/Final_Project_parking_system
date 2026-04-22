package com.example.demo.domain.management.dtos.response;

import com.example.demo.domain.vehicle.Vehicle;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AdminVehicleResponseDto {
    private Long vehicleId;
    private String carNumber;
    private String vehicleName;
    private String ownerName;
    private String status;
    private LocalDateTime createdAt;

    public static AdminVehicleResponseDto from(Vehicle vehicle){
        return AdminVehicleResponseDto.builder()
                .vehicleId(vehicle.getId())
                .carNumber(vehicle.getCarNumber())
                .vehicleName(vehicle.getVehicleName())
                .ownerName(vehicle.getUser() != null ? vehicle.getUser().getName() : "소유자 없음")
                .status(vehicle.getStatus().name())
                .createdAt(vehicle.getCreatedAt())
                .build();
    }
}
