package com.example.demo.domain.resident.household.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum IsActive {

    ACTIVE("활성"),
    INACTIVE("비활성");

    private final String description;
}