package com.example.demo.domain.kiosk.payment.dtos.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class VehiclePaymentResponseDto {
    private boolean isFree;      // 무료 통과 여부
    private Integer fee;         // 결제해야 할 금액
    private String message;      // 화면에 띄울 메시지 (예: "정기권 차량입니다. 안녕히 가세요!")
}
