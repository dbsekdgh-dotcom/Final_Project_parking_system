package com.example.demo.domain.user.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Status {

    ACTIVE("활성"),
    DELETE("삭제");

    private final String description;
}
