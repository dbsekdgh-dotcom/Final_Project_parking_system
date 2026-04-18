package com.example.demo.domain.parking.entry.dtos.response;

import com.example.demo.domain.parking.space.ParkingSpace;
import lombok.Getter;

@Getter
public class ParkingSpaceResponse {
    private final Long id;
    private final String spaceCode;
    private final String floor;
    private final String status;
    private final Boolean isDisabled;
    private final Boolean isEvCharge;
    private final Boolean isReservation;

    public ParkingSpaceResponse(ParkingSpace ps) {
        this.id = ps.getId();
        this.spaceCode = ps.getSpaceCode();
        this.floor = ps.getFloor().name();
        this.status = ps.getStatus().name();
        this.isDisabled = ps.getIsDisabled();
        this.isEvCharge = ps.getIsEvCharge();
        this.isReservation = ps.getIsReservation();
    }
}
