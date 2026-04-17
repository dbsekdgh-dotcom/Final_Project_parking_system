package com.example.demo.domain.user.vehicle.dtos.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class VehicleCancelRequestDto {
    @NotNull(message = "취소할 차량의 ID는 필수입니다.")
    private Long vehicleId;
}
