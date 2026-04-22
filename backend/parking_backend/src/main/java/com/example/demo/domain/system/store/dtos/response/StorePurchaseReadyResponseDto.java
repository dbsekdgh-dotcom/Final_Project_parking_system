package com.example.demo.domain.system.store.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class StorePurchaseReadyResponseDto {
    private String orderId;
    private String orderName;
    private Integer amount;

}
