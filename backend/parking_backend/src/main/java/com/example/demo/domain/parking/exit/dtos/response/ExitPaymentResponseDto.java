package com.example.demo.domain.parking.exit.dtos.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ExitPaymentResponseDto {
    private long parkingLogId;

    @JsonProperty("isFree")
    private boolean isFree;
    private String message;
    private String vehicleNumber;
    private long parkingTime;
    private Integer rawFee;
    private long calculatedFee;
    private long amountToPay;
    private int userPoint;
}
