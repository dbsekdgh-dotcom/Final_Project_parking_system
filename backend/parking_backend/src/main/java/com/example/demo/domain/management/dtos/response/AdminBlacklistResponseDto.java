package com.example.demo.domain.management.dtos.response;

import com.example.demo.domain.vehicle.blacklist.VehicleBlacklist;
import com.example.demo.domain.vehicle.blacklist.enums.BlacklistReasonType;
import com.example.demo.domain.vehicle.blacklist.enums.BlacklistStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AdminBlacklistResponseDto {

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

    public static AdminBlacklistResponseDto from(VehicleBlacklist bl) {
        return AdminBlacklistResponseDto.builder()
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
                .build();
    }
}
