package com.example.demo.domain.system.store.dtos.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class StoreLoginRequestDto {
    private Long storeId;
    private String terminalPassword;
}
