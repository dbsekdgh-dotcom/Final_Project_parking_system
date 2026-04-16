package com.example.demo.domain.user.vehicle.dtos.response;

import com.example.demo.domain.shared.vehicle.enums.VehicleStatus; // Enum 임포트 추가
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class VehicleResponseDto {

    private Long vehicleId;

    private String carNumber;

    private String vehicleName;

    private VehicleStatus status;

    private LocalDateTime createdAt;
}