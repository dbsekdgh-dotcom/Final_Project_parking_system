package com.example.demo.domain.vehicle.dtos.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "차량 등록 신청 취소 요청 데이터 — PENDING 상태 차량의 ID를 전달합니다.")
public class VehicleCancelRequestDto {
    @NotNull(message = "취소할 차량의 ID는 필수입니다.")
    private Long vehicleId;
}
