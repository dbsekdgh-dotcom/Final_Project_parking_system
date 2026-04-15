package com.example.demo.domain.kiosk.payment.dtos.internal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@ToString
public class StackableTicketResult {
    private int totalAmount; //최종 금액할인
    private List<AppliedTicketResult> details=new ArrayList<>();
}
