package com.example.demo.domain.kiosk.parking.exit.enums;

public enum ParkingStatus {
    DETECTED("감지됨"),
    ENTRY_CANCELLED("입차취소"),
    ENTERED("입차완료"),
    EXIT_REQUESTED("출차요청"),
    EXITED("출차완료"),
    FORCE_EXITED("강제출차");

    private final String description;

    ParkingStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
