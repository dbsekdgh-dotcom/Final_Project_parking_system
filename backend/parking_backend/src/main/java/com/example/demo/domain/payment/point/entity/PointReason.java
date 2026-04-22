package com.example.demo.domain.payment.point.entity;

public enum PointReason {
    PAYMENT_EARN,
    PAYMENT_USE,
    REFUND,
    ADMIN_GRANT,
    ADMIN_REVOKE;

    public boolean isDeduction(){
        // 이제 REFUND는 여기서 뺍니다.
        // 단순히 적립/차감 여부는 Service에서 호출하는 메서드(earn/use)가 결정하게 둡니다.
        return this == PAYMENT_USE || this == ADMIN_REVOKE;
    }}
