package com.example.demo.domain.kiosk.payment.dtos.request;

import com.example.demo.domain.shared.parkingTicket.ParkingTicket;
import com.example.demo.domain.shared.parkingfeepolicy.ParkingFeePolicy;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;


@Getter
@Builder
public class FeeCalculationRequestDto {
    private long parkingTime;
    private ParkingFeePolicy policy;
    private int prepaidFee;
    private List<ParkingTicket> discountTicketRequestDtos;
    private String vehicleNumber;
    private LocalDateTime freeExitUntil;
}
