package com.example.demo.domain.vehicle.dtos.response;

import com.example.demo.domain.vehicle.enums.VehicleStatus; // Enum 임포트 추가
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "내 차량 정보 응답 — 차량 ID, 차량 번호, 차종, 상태(ACTIVE/PENDING/DELETED), 등록일시를 반환합니다.")
public class VehicleResponseDto {

    private Long vehicleId;

    private Long approvalId;

    private String carNumber;

    private String vehicleName;

    private VehicleStatus status;

    private LocalDateTime createdAt;

    private boolean isBlacklisted;
}