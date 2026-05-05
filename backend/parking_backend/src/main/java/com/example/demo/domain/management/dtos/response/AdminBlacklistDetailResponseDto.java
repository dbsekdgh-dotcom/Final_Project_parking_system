package com.example.demo.domain.management.dtos.response;

import com.example.demo.domain.vehicle.blacklist.enums.BlacklistReasonType;
import com.example.demo.domain.vehicle.blacklist.enums.BlacklistStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Builder
public class AdminBlacklistDetailResponseDto {
    private Long id;
    private String carNumber;
    private BlacklistReasonType reasonType;
    private String reasonDetail;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private BlacklistStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime releasedAt;

    private Long vehicleId;
    private String vehicleName;
    private String ownerName;
    private String ownerEmail;

    public boolean isPermanent(){
        return endDate != null && endDate.getYear() >=3000;
    }
}
