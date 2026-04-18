package com.example.demo.domain.parking.log.dtos.response;

import com.example.demo.domain.payment.ticketpolicy.enums.DiscountType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminTicketPolicyResponse {
    private Long id; // 할인 정책ID
    private String name; // 할인권 이름
    private DiscountType discountType; // 할인 방식(TIME,AMOUNT,FREE)
    private Integer discountValue; // 할인 값
}
