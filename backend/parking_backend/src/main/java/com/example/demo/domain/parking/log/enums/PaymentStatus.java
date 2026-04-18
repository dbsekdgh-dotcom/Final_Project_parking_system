package com.example.demo.domain.parking.log.enums;

public enum PaymentStatus {
    NONE("결제 불필요"),
    UNPAID("미결제"),
    PAID("결제완료"),
    REFUNDED("환불완료");

    private final String description;

    PaymentStatus(String description){
        this.description=description;
    }
    public String getDescription(){
        return description;
    }
}
