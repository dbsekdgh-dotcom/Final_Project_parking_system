package com.example.demo.domain.payment.dtos.request;

import com.example.demo.domain.payment.dtos.internal.AppliedTicketResult;
import com.example.demo.domain.payment.dtos.internal.StackableTicketResult;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
@Builder
public class SettlementRequestDto {
    private Long parkingLogId;
    private String vehicleNumber;
    private int usedPoint;
    private int paidAmount;
    private String settlementType; //kiosk(사전정산) or EXIT_GATE(사후정산)
}
