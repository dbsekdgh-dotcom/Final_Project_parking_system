package com.example.demo.domain.shared.activityLog.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ActivityType {
    ENTRY("입차"),
    EXIT("출차"),
    PAYMENT_PRE("사전정산"),
    PAYMENT_EXIT("출차정산"),
    PAYMENT_CANCEL("결제취소"),
    RESERVATION_CREATED("방문예약 생성"),
    RESERVATION_CANCELLED("방문예약 취소"),
    RESIDENT_REGISTERED("입주민 등록"),
    VEHICLE_REGISTERED("차량 등록"),
    PASS_PURCHASED("정기권 구매"),
    COUPON_PURCHASED("할인권 구매"),
    COUPON_USED("할인 적용"),
    ADMIN_FORCE_EXIT("관리자 강제출차");

    private final String description;
}
