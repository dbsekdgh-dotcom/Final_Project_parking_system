package com.example.demo.domain.management.dtos.response;

import com.example.demo.domain.vehicle.blacklist.VehicleBlacklist;
import com.example.demo.domain.vehicle.blacklist.enums.BlacklistReasonType;
import com.example.demo.domain.vehicle.blacklist.enums.BlacklistStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AdminBlacklistDetailResponseDto {

    private Long id;
    private String carNumber;
    private BlacklistReasonType reasonType;
    private String reasonDetail;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private boolean permanent;
    private BlacklistStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime releasedAt;

    // 연결된 차량 정보 (비회원이면 null)
    private Long vehicleId;
    private String vehicleName;
    private String ownerName;
    private String ownerEmail;

    public static AdminBlacklistDetailResponseDto from(VehicleBlacklist bl) {
        String vehicleName = null;
        String ownerName   = null;
        String ownerEmail  = null;
        Long   vehicleId   = null;

        if (bl.getVehicle() != null) {
            vehicleId   = bl.getVehicle().getId();
            vehicleName = bl.getVehicle().getVehicleName();
            if (bl.getVehicle().getUser() != null) {
                ownerName  = bl.getVehicle().getUser().getName();
                ownerEmail = bl.getVehicle().getUser().getEmail();
            }
        }

        return AdminBlacklistDetailResponseDto.builder()
                .id(bl.getId())
                .carNumber(bl.getCarNumber())
                .reasonType(bl.getReasonType())
                .reasonDetail(bl.getReasonDetail())
                .startDate(bl.getStartDate())
                .endDate(bl.getEndDate())
                .permanent(bl.getEndDate().getYear() >= 3000)
                .status(bl.getStatus())
                .createdAt(bl.getCreatedAt())
                .releasedAt(bl.getReleasedAt())
                .vehicleId(vehicleId)
                .vehicleName(vehicleName)
                .ownerName(ownerName)
                .ownerEmail(ownerEmail)
                .build();
    }
}
