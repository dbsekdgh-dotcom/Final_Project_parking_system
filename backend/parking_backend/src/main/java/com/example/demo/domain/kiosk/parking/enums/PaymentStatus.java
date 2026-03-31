package com.example.demo.domain.kiosk.parking.enums;

public enum PaymentStatus {
    NONE("결제 불필요"),
    UNPAID("미결제"),
    PAID("결제완료");

    private final String description;

    PaymentStatus(String description){
        this.description=description;
    }
    public String getDescription(){
        return description;
    }
}
