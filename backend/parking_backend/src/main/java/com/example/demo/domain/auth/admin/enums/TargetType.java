package com.example.demo.domain.auth.admin.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TargetType {
    USER("유저"),
    VEHICLE("차량"),
    PAYMENT("결제"),
    POLICY("정책"),
    RESERVATION("예약"),
    SYSTEM_SETTING("시스템설정"),
    STORE("상가"),
    PARKING_LOG("주차로그"),
    PARKING_SPACE("주차공간"),
    REPORT("신고"),
    RESERVATION_POLICY("방문예약 정책");

    private final String description;
}
