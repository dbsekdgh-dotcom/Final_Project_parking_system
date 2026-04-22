package com.example.demo.domain.system.store.dtos.response;

import com.example.demo.domain.parking.log.ParkingLog;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class StoreCarSearchResponseDto {
    private Long parkingLogId;
    private String carNumber;
    private LocalDateTime entryTime;
    private Long calculatedFee;

    public static StoreCarSearchResponseDto from(ParkingLog log){
        return StoreCarSearchResponseDto.builder()
                .parkingLogId(log.getParkingLogId())
                .carNumber(log.getCarNumberSnapshot())
                .entryTime(log.getEnteredAt())
                .calculatedFee(log.getCalculatedFee())
                .build();
    }
}
