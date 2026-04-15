package com.example.demo.domain.admin.management.parkingSpace.dtos.response;

import com.example.demo.domain.shared.parkingspace.enums.SpaceStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ParkingSpaceListResponse {
    private Long id; //parking_space_id
    private String spaceCode;
    private SpaceStatus status; //AVAILABLE,OCCUPIED,BLOCKED
    private boolean isDisabled;
    private boolean isEvCharge;
}
