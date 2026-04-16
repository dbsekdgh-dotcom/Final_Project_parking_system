package com.example.demo.domain.user.vehicle.dtos.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class VehicleOcrResponseDto {

    private final String carNumber;
    private final String vehicleName;
    private final String name;
    private final String birth;

    private final String ocrRawName;
    private final String ocrRawBirth;

}
