package com.example.demo.domain.parking.space.dtos.response;

import com.example.demo.domain.parking.space.ParkingSpace;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ParkingSpaceLogSnapShot {
    private String spaceCode;
    private String status;
    @JsonProperty("isDisabled")
    private boolean isDisabled;
    @JsonProperty("isEvCharge")
    private boolean isEvCharge;

    // Entity를 Dto로 변환
    public static ParkingSpaceLogSnapShot from(ParkingSpace space){
        return ParkingSpaceLogSnapShot.builder()
                .spaceCode(space.getSpaceCode())
                .status(space.getStatus().name())
                .isDisabled(space.getIsDisabled())
                .isEvCharge(space.getIsEvCharge())
                .build();
    }
}
