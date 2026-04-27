package com.example.demo.domain.management.store.dtos.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class StoreUpdateRequestDto {
    private String name;
    private String terminalPassword;
}
