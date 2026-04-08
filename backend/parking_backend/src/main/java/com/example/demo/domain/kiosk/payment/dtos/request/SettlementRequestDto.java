package com.example.demo.domain.kiosk.payment.dtos.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
public class SettlementRequestDto {
    private Long parkingLogId;
    private String vehicleNumber;
    private int usedPoint;
    private int paidAmount;
    private String settlementType; //kiosk(사전정산) or EXIT_GATE(사후정산)
}
