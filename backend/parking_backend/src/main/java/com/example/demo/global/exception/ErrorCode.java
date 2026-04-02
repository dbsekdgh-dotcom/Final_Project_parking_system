package com.example.demo.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    // COMMON
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
    ENTITY_NOT_FOUND(HttpStatus.NOT_FOUND, "데이터를 찾을 수 없습니다."),

    // VEHICLE
    VEHICLE_NOT_FOUND(HttpStatus.NOT_FOUND, "등록된 차량이 없습니다."),
    DUPLICATE_VEHICLE(HttpStatus.CONFLICT, "이미 등록된 차량입니다."),

    // PARKING
    PARKING_FULL(HttpStatus.CONFLICT, "주차장이 만차입니다."),
    VEHICLE_ALREADY_ENTERED(HttpStatus.CONFLICT, "이미 입차된 차량입니다."),
    VEHICLE_NOT_ENTERED(HttpStatus.BAD_REQUEST, "입차 기록이 없습니다."),

    // PAYMENT
    PAYMENT_NOT_COMPLETED(HttpStatus.BAD_REQUEST, "결제가 완료되지 않았습니다."),
    INVALID_PAYMENT_AMOUNT(HttpStatus.BAD_REQUEST, "결제 금액이 올바르지 않습니다."),

    // AUTH
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),

    //EXIT & SETTLEMENT
    ALREADY_EXITED(HttpStatus.BAD_REQUEST,"이미 출차가 완료된 차량입니다."),
    BLACKLIST_VEHICLE(HttpStatus.FORBIDDEN, "제한된 차량입니다. 관리자에게 문의하세요."),
    GRACE_PERIOD_EXCEEDED(HttpStatus.PAYMENT_REQUIRED, "회차 시간이 초과되어 요금이 발생했습니다.");


    private final HttpStatus status;
    private final String message;
}
