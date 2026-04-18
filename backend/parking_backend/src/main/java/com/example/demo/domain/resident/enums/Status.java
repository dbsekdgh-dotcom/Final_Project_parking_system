package com.example.demo.domain.resident.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Status {

    ACTIVE("활성"),
    DELETED("삭제");

    private final String description;
}
