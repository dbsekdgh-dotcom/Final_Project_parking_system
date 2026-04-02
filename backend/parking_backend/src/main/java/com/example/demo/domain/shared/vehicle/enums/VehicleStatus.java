package com.example.demo.domain.shared.vehicle.enums;

public enum VehicleStatus {
    ACTIVE("활성화"),
    DELETED("삭제됨");

    private final String description;

    VehicleStatus(String description){
        this.description=description;
    }
    public String getDescription(){
        return description;
    }
}
