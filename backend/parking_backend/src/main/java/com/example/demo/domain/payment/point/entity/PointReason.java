package com.example.demo.domain.payment.point.entity;

public enum PointReason {
    PAYMENT_EARN,
    PAYMENT_USE,
    REFUND,
    ADMIN_GRANT,
    ADMIN_REVOKE;

    public boolean isDeduction(){
        return this==PAYMENT_USE || this==REFUND || this==ADMIN_REVOKE;
    }
}
