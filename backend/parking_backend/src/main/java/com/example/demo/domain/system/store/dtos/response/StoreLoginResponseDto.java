package com.example.demo.domain.system.store.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class StoreLoginResponseDto {
    private Long storeId;
    private String storeName;
    private String token;
}
