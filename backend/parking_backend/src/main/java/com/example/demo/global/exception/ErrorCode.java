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
    ALREADY_PROCESSING(HttpStatus.CONFLICT,"이미 결제가 진행 중인 차량입니다."),
    PAYMENT_TIMEOUT(HttpStatus.BAD_REQUEST,"결제 제한 시간이 초과되었습니다."),
    ALREADY_PAID(HttpStatus.BAD_REQUEST,"이미 정산이 완료된 차량입니다."),
    ALREADY_EXITED(HttpStatus.GONE,"이미 출차가 완료된 차량입니다."),
    BLACKLIST_VEHICLE(HttpStatus.FORBIDDEN, "제한된 차량입니다. 관리자에게 문의하세요."),
    GRACE_PERIOD_EXCEEDED(HttpStatus.PAYMENT_REQUIRED, "회차 시간이 초과되어 요금이 발생했습니다."),
    NOT_PAYMENT_TARGET(HttpStatus.BAD_REQUEST, "정산 대상 차량이 아닙니다."),

    // AUTH (로그인 및 회원가입 관련 추가)
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),
    LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 일치하지 않습니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "가입되지 않은 이메일 주소입니다."),
    EMAIL_DUPLICATE(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "만료된 토큰입니다."),
    ACCOUNT_DISABLED(HttpStatus.FORBIDDEN, "사용할 수 없는 계정입니다. 관리자에게 문의하세요."),

    // AUTH (계정 연동 및 비밀번호 관련 추가)
    PASSWORD_MISMATCH(HttpStatus.BAD_REQUEST, "비밀번호가 일치하지 않습니다."),
    ALREADY_LINKED_LOCAL(HttpStatus.CONFLICT, "이미 로컬 계정이 연동되어 있습니다."),
    SOCIAL_LINK_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "소셜 계정 연동 중 오류가 발생했습니다."),
    PHONE_DUPLICATE(HttpStatus.CONFLICT, "이미 등록된 전화번호입니다."), // 아까 말한 폰 유니크 제약용

    //AI
    AI_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR,"서비스 호출에 실패하였습니다."),
    PG_PROVIDER_ERROR(HttpStatus.BAD_GATEWAY,"결제 서비스 호출에 실패하였습니다."),
    REDIS_CONNECTION_FAILURE(HttpStatus.SERVICE_UNAVAILABLE,"실시간 서비스 이용이 불가능합니다."),

    // [추가] 정의되지 않은 모든 서버 에러를 위한 공통 코드
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다. 관리자에게 문의하세요.")
    ;


    private final HttpStatus status;
    private final String message;
}
