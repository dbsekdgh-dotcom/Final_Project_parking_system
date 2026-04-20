package com.example.demo.domain.parking.policy.dtos.request;

import com.example.demo.domain.parking.policy.enums.ParkingType;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@Builder
@ToString
@NoArgsConstructor
public class ParkingFeePolicyUpdateRequestDto {
    private ParkingType parkingType;
    private int graceMinutes;
    private int baseFee;
    private int unitMinutes;
    private int unitFee;
    private int daliyMaxFee;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime effectiveFrom;
}
