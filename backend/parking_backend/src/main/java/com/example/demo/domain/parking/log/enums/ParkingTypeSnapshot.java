package com.example.demo.domain.parking.log.enums;

public enum ParkingTypeSnapshot {
    RESIDENT("입주민"),
    SUBSCRIPTION("정기권"),
    RESERVATION("예약방문"),
    USER("회원"),
    VISIT("외부");

    private final String description;

    ParkingTypeSnapshot(String description){
        this.description=description;
    }
    public String getDescription(){
        return description;
    }
}
