package com.example.demo.domain.user.vehicle.dtos.response;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class VehicleOcrResultDto {

    private final String carNumber;
    private final String vehicleName;
    private final String name;
    private final String birth;

    private final String ocrRawName;
    private final String orcRawBirth;
}
