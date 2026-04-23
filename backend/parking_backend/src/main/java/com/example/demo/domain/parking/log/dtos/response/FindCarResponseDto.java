package com.example.demo.domain.parking.log.dtos.response;

import com.example.demo.domain.parking.log.ParkingLog;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class FindCarResponseDto {
    private Long parkingLogId;
    private String carNumber;
    private String spaceCode;
    private String floor;
    private LocalDateTime enteredAt;

    public static FindCarResponseDto from(ParkingLog log){
        String spaceCode = (log.getParkingSpace() != null) ? log.getParkingSpace().getSpaceCode() : " 미배정 ";
        String floor = (log.getParkingSpace() != null) ? log.getParkingSpace().getFloor().name() : "-";

        return FindCarResponseDto.builder()
                .parkingLogId(log.getParkingLogId())
                .carNumber(log.getCarNumberSnapshot())
                .spaceCode(spaceCode)
                .floor(floor)
                .enteredAt(log.getEnteredAt())
                .build();
    }
}
