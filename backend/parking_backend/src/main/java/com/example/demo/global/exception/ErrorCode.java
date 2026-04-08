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
    SPACE_NOT_AVAILABLE(HttpStatus.CONFLICT, "선택한 자리를 사용할 수 없습니다."),
    VEHICLE_ALREADY_ENTERED(HttpStatus.CONFLICT, "이미 입차된 차량입니다."),
    VEHICLE_NOT_ENTERED(HttpStatus.BAD_REQUEST, "입차 기록이 없습니다."),
    PARKING_POLICY_NOT_FOUND(HttpStatus.NOT_FOUND,"정책을 찾을 수 없습니다."),
    CAMERA_NOT_FOUND(HttpStatus.NOT_FOUND,"카메라를 찾을 수 없습니다."),
    PARKING_LOG_NOT_FOUND(HttpStatus.NOT_FOUND,"주차 세션을 찾을 수 없습니다."),

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
    WITHDRAWN_ACCOUNT(HttpStatus.FORBIDDEN, "탈퇴한 계정입니다. 복구하시겠습니까?"),
    TOO_MANY_LOGIN_ATTEMPTS(HttpStatus.FORBIDDEN, "로그인 시도 횟수가 초과되었습니다. 5분 뒤에 다시 시도하거나 이메일 인증을 통해 차단을 해제해 주세요."),

    // AUTH (계정 연동 및 비밀번호 관련 추가)
    PASSWORD_MISMATCH(HttpStatus.BAD_REQUEST, "비밀번호가 일치하지 않습니다."),
    ALREADY_LINKED_LOCAL(HttpStatus.CONFLICT, "이미 로컬 계정이 연동되어 있습니다."),
    SOCIAL_LINK_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "소셜 계정 연동 중 오류가 발생했습니다."),
    PHONE_DUPLICATE(HttpStatus.CONFLICT, "이미 등록된 전화번호입니다."), // 아까 말한 폰 유니크 제약용
    SOCIAL_USER_LOGIN_ATTEMPT(HttpStatus.CONFLICT, "소셜 계정으로 가입된 사용자입니다. 소셜 로그인을 이용해 주세요."),

    // AUTH (인증번호 관련 추가)
    USER_INFORMATION_MISMATCH(HttpStatus.NOT_FOUND, "입력하신 정보와 일치하는 탈퇴 계정을 찾을 수 없습니다. 이름, 이메일, 휴대폰 번호를 다시 확인해 주세요."),    VERIFICATION_CODE_MISMATCH(HttpStatus.BAD_REQUEST, "인증번호가 일치하지 않습니다."),
    VERIFICATION_CODE_EXPIRED(HttpStatus.GONE, "인증 시간이 초과되었습니다. 다시 시도해 주세요."),
    MAIL_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "이메일 발송 중 오류가 발생했습니다."),
    UNAUTHORIZED_ACCESS(HttpStatus.FORBIDDEN, "인증되지 않은 접근입니다. 먼저 이메일 인증을 완료해주세요."),

    // AUTH (회원 탈퇴 관련 추가)
    INVALID_CONFIRM_TEXT(HttpStatus.BAD_REQUEST, "탈퇴 확인 문구가 일치하지 않습니다."),
    WITHDRAW_PASSWORD_MISMATCH(HttpStatus.BAD_REQUEST, "현재 비밀번호가 일치하지 않습니다."),
    RESTORE_TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "복구 인증 세션이 만료되었습니다."), // 복구 진행 중 단계가 끊겼을 때

    // AUTH (계정 복구 관련 추가)
    RECOVERY_NOT_FOUND(HttpStatus.NOT_FOUND, "복구 가능한 탈퇴 기록이 없습니다."),
    INVALID_AUTH_CODE(HttpStatus.BAD_REQUEST, "인증번호가 일치하지 않습니다."), // 기존 VERIFICATION_CODE_MISMATCH와 통합 가능하지만 명확히 분리 시 사용
    PHONE_ALREADY_ACTIVE(HttpStatus.CONFLICT, "이미 동일한 번호로 사용 중인 계정이 있어 복구가 불가능합니다."),
    SOCIAL_RECOVERY_PHONE_CONFLICT(HttpStatus.CONFLICT, "해당 전화번호로 가입된 활성 계정이 있어 소셜 복구가 불가능합니다."),
    ALREADY_LINKED_SOCIAL(HttpStatus.CONFLICT, "해당 소셜 계정은 이미 다른 서비스 계정과 연동되어 있습니다."),
    INVALID_USER_DATA_FORMAT(HttpStatus.INTERNAL_SERVER_ERROR, "사용자 데이터 형식이 올바르지 않아 처리가 불가능합니다. 관리자에게 문의하세요."),

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
