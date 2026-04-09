package com.example.demo.domain.shared.approval.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ApprovalType {

    RESIDENT("입주민 신청"),
    VEHICLE("차량등록"),
    RESERVATION("방문예약"),
    REPORT("신고 처리");

    private final String description;
}
