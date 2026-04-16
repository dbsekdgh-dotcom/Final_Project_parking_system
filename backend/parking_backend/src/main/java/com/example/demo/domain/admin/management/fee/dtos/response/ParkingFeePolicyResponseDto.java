package com.example.demo.domain.admin.management.fee.dtos.response;

import com.example.demo.domain.shared.parkingfeepolicy.ParkingFeePolicy;
import com.example.demo.domain.shared.parkingfeepolicy.enums.ParkingType;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class ParkingFeePolicyResponseDto {
    private long parkingFeePolicyId;
    private long adminId;
    private ParkingType parkingType;
    private int graceMinutes;
    private int baseFee;
    private int unitMinutes;
    private int unitFee;
    private int daliyMaxFee;
    private boolean isActive;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime effectiveFrom;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime effectiveTo;
    private long version;

    public static ParkingFeePolicyResponseDto toPolicyDto(ParkingFeePolicy parkingFeePolicy){
        return ParkingFeePolicyResponseDto.builder()
                .parkingFeePolicyId(parkingFeePolicy.getId())
                .adminId(parkingFeePolicy.getAdmin().getAdminId())
                .parkingType(parkingFeePolicy.getParkingType())
                .graceMinutes(parkingFeePolicy.getGraceMinutes())
                .baseFee(parkingFeePolicy.getBaseFee())
                .unitMinutes(parkingFeePolicy.getUnitMinutes())
                .unitFee(parkingFeePolicy.getUnitMinutes())
                .daliyMaxFee(parkingFeePolicy.getDailyMaxFee())
                .isActive(parkingFeePolicy.getIsActive())
                .effectiveFrom(parkingFeePolicy.getEffectiveFrom())
                .effectiveTo(parkingFeePolicy.getEffectiveTo())
                .version(parkingFeePolicy.getVersion())
                .build();
    }
}
