package com.example.demo.domain.vehicle.blacklist.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BlacklistReasonType {
    REPORT_ACCUMULATION("신고 누적"),
    ILLEGAL_VEHICLE("불법 차량"),
    USER_BLACKLIST("사용자 차단"),
    ADMIN_MANUAL("관리자 직접 등록"),
    SYSTEM_BLOCK("시스템 차단");

    private final String description;
}
