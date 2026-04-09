package com.example.demo.domain.shared.approval.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ApprovalStatus {

    PENDING("대기"),
    APPROVED("승인"),
    REJECTED("거절"),
    CANCELLED("취소");

    private final String description;
}
