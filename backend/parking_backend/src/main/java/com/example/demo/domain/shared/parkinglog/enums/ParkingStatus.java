package com.example.demo.domain.shared.parkinglog.enums;

public enum ParkingStatus {
    DETECTED("감지됨"),
    ENTRY_CANCELLED("입차취소"),
    ENTERED("입차완료"),
    EXIT_REQUESTED("출차요청"),
    EXITED("출차완료"),
    FORCE_EXITED("강제출차"),
    BLACKLIST_REJECTED("차단 차량 거부");

    private final String description;

    ParkingStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean canTransitTo(ParkingStatus next){
        return switch (this){
            case DETECTED ->
                next == ENTERED || next == ENTRY_CANCELLED;
            case ENTERED ->
                next == EXIT_REQUESTED || next == FORCE_EXITED;
            case EXIT_REQUESTED ->
                next == EXITED || next == FORCE_EXITED || next == ENTERED;
            default -> false;
        };
    }
    public boolean isActiveSession(){
        return this == DETECTED || this==ENTERED||this==EXIT_REQUESTED;
    }
    public boolean isFinished(){
        return this == EXITED || this==ENTRY_CANCELLED || this==FORCE_EXITED || this==BLACKLIST_REJECTED;
    }
}
