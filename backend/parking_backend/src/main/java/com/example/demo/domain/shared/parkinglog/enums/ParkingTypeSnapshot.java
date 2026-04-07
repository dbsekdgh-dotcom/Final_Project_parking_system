package com.example.demo.domain.shared.parkinglog.enums;

public enum ParkingTypeSnapshot {
    RESIDENT("입주민"),
    VISIT("외부"),
    USER("회원"),
    RESERVATION("예약방문"),
    SUBSCRIPTION("정기권");

    private final String description;

    ParkingTypeSnapshot(String description){
        this.description=description;
    }
    public String getDescription(){
        return description;
    }
}
