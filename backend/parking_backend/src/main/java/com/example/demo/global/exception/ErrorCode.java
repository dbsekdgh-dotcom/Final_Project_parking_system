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
    REPORT_NOT_FOUND(HttpStatus.NOT_FOUND, "신고 내역을 찾을 수 없습니다."),
    REPORT_CANNOT_CANCEL(HttpStatus.FORBIDDEN, "본인 신고만 취소 가능합니다."),
    USER_NOT_FOUND_REPORT(HttpStatus.NOT_FOUND, "유저를 찾을 수 없습니다."),
    VEHICLE_SIMILARITY_TOO_LOW(HttpStatus.BAD_REQUEST, "등록 정보가 일치하지 않습니다. 관리자 승인이 필요합니다."),
    INVALID_CAR_NUMBER_FORMAT(HttpStatus.BAD_REQUEST, "올바른 차량 번호 형식이 아닙니다."),
    OCR_DATA_MISSING(HttpStatus.BAD_REQUEST, "OCR 인식 결과 데이터가 누락되었습니다."),
    VEHICLE_ALREADY_PENDING(HttpStatus.CONFLICT, "이미 승인 대기 중인 차량 번호입니다."),


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
    PAYMENT_ALREADY_PROCESSED(HttpStatus.CONFLICT, "이미 처리된 결제건입니다."),
    PAYMENT_TIMEOUT(HttpStatus.BAD_REQUEST,"결제 제한 시간이 초과되었습니다."),
    ALREADY_PAID(HttpStatus.BAD_REQUEST,"이미 정산이 완료된 차량입니다."),
    ALREADY_EXITED(HttpStatus.GONE,"이미 출차가 완료된 차량입니다."),
    BLACKLIST_VEHICLE(HttpStatus.FORBIDDEN, "제한된 차량입니다. 관리자에게 문의하세요."),
    GRACE_PERIOD_EXCEEDED(HttpStatus.PAYMENT_REQUIRED, "회차 시간이 초과되어 요금이 발생했습니다."),
    NOT_PAYMENT_TARGET(HttpStatus.BAD_REQUEST, "정산 대상 차량이 아닙니다."),
    MINIMUM_POINT_NOT_ME(HttpStatus.BAD_REQUEST,"포인트는 100원부터 사용 가능합니다."),
    FORCE_EXITED(HttpStatus.BAD_REQUEST,"관리자에 의해 출차가 완료된 차량입니다."),
    PAYMENT_NETWORK_ERROR(HttpStatus.BAD_REQUEST,"결제 시스템 오류로 결제에 실패하였습니다."),
    INVALID_REFUND_AMOUNT(HttpStatus.BAD_REQUEST, "환불 금액이 올바르지 않습니다."),
    LOCK_ACQUISITION_FAILED(HttpStatus.SERVICE_UNAVAILABLE, "현재 결제 요청이 많아 처리가 지연되고 있습니다. 잠시 후 다시 시도해 주세요."),
    INSUFFICIENT_POINTS(HttpStatus.BAD_REQUEST,"포인트 잔액이 부족합니다."),

    // AUTH (로그인 및 회원가입 관련 추가)
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),
    LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 일치하지 않습니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "가입되지 않은 이메일 주소입니다."),
    EMAIL_DUPLICATE(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "만료된 토큰입니다."),
    ACCOUNT_DISABLED(HttpStatus.FORBIDDEN, "사용할 수 없는 계정입니다. 관리자에게 문의하세요."),
    WITHDRAWN_ACCOUNT(HttpStatus.CONFLICT, "탈퇴한 계정입니다. 복구하시겠습니까?"),    TOO_MANY_LOGIN_ATTEMPTS(HttpStatus.FORBIDDEN, "로그인 시도 횟수가 초과되었습니다. 5분 뒤에 다시 시도하거나 이메일 인증을 통해 차단을 해제해 주세요."),

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
    USER_SUSPENDED(HttpStatus.FORBIDDEN, "제재된 사용자입니다. 관리자에게 문의하세요."),
    // AUTH (계정 복구 관련 추가)
    RECOVERY_NOT_FOUND(HttpStatus.NOT_FOUND, "복구 가능한 탈퇴 기록이 없습니다."),
    INVALID_AUTH_CODE(HttpStatus.BAD_REQUEST, "인증번호가 일치하지 않습니다."), // 기존 VERIFICATION_CODE_MISMATCH와 통합 가능하지만 명확히 분리 시 사용
    PHONE_ALREADY_ACTIVE(HttpStatus.CONFLICT, "이미 동일한 번호로 사용 중인 계정이 있어 복구가 불가능합니다."),
    SOCIAL_RECOVERY_PHONE_CONFLICT(HttpStatus.CONFLICT, "해당 전화번호로 가입된 활성 계정이 있어 소셜 복구가 불가능합니다."),
    ALREADY_LINKED_SOCIAL(HttpStatus.CONFLICT, "해당 소셜 계정은 이미 다른 서비스 계정과 연동되어 있습니다."),
    INVALID_USER_DATA_FORMAT(HttpStatus.INTERNAL_SERVER_ERROR, "사용자 데이터 형식이 올바르지 않아 처리가 불가능합니다. 관리자에게 문의하세요."),

    // APPLY (입주 신청 관련 추가)
    ALREADY_APPLIED_RESIDENT(HttpStatus.CONFLICT, "이미 대기 중인 입주 신청 내역이 있습니다."),
    ALREADY_RESIDENT(HttpStatus.CONFLICT, "이미 다른 세대에 거주 중인 입주민입니다."), // 유저가 이미 집이 있는 경우
    NOT_AVAILABLE_HOUSEHOLD(HttpStatus.BAD_REQUEST, "현재 신청 가능한 상태가 아닌 호수입니다."), // PENDING 상태인 호수 포함
    APPLY_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 신청 내역을 찾을 수 없거나 취소 권한이 없습니다."),
    CANNOT_CANCEL_APPROVED(HttpStatus.BAD_REQUEST, "이미 승인 또는 거절된 신청은 취소할 수 없습니다."),
    NOT_RESIDENT_USER(HttpStatus.FORBIDDEN, "입주민 승인 후 이용 가능한 서비스입니다."),

    // HOUSEHOLD 섹션
    HOUSEHOLD_COUNT_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "세대별 예약 카운트 처리에 오류가 발생했습니다."),
    HOUSEHOLD_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 호수입니다."), // DB에 미리 넣었더라도 잘못된 ID/번호 요청 대응
    HOUSEHOLD_ALREADY_ACTIVE(HttpStatus.CONFLICT, "해당 세대는 이미 입주가 완료되었습니다."),


    // APPROVAL 섹션 (승인/거절 시 필요)
    APPROVAL_NOT_FOUND(HttpStatus.NOT_FOUND, "결재 정보를 찾을 수 없습니다."),
    ALREADY_PROCESSED_APPROVAL(HttpStatus.CONFLICT, "이미 처리된 결재 건입니다."),
    NOT_AUTHORIZED_APPROVER(HttpStatus.FORBIDDEN, "해당 결재를 처리할 권한이 없습니다."),

    // RESERVATION (방문 예약 관련 추가)
    RESERVATION_NOT_FOUND(HttpStatus.NOT_FOUND, "예약 내역을 찾을 수 없습니다."),
    ALREADY_RESERVED_VEHICLE(HttpStatus.CONFLICT, "해당 차량은 이미 예약이 진행 중입니다."),
    ACTIVE_SUBSCRIPTION_EXISTS(HttpStatus.CONFLICT, "이미 정기권이 등록된 차량입니다. 별도의 방문 예약이 필요하지 않습니다."),
    MAX_RESERVATION_EXCEEDED(HttpStatus.CONFLICT, "동시에 보유 가능한 활성 예약 수를 초과했습니다."),
    DAILY_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "일일 예약 가능 횟수를 초과했습니다."),
    MONTHLY_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "월간 예약 가능 횟수를 초과했습니다."),
    INVALID_RESERVATION_TIME(HttpStatus.BAD_REQUEST, "예약 시간 설정이 올바르지 않습니다."),
    CANNOT_CANCEL_RESERVATION(HttpStatus.BAD_REQUEST, "현재 상태에서는 예약을 취소할 수 없습니다."),
    RESERVATION_ALREADY_CANCELLED(HttpStatus.CONFLICT, "이미 취소된 예약입니다."),
    RESERVATION_ALREADY_USED(HttpStatus.BAD_REQUEST, "이미 입차된 예약은 취소할 수 없습니다."),
    RESERVATION_NOT_OWNER(HttpStatus.FORBIDDEN, "본인의 예약만 취소할 수 있습니다."),
    RESERVATION_STATUS_MISMATCH(HttpStatus.BAD_REQUEST, "취소 가능한 상태의 예약이 아닙니다."),
    RESERVATION_CANNOT_CANCEL_STATUS(HttpStatus.BAD_REQUEST, "이미 사용 중이거나 완료된 예약은 취소할 수 없습니다."), // STATUS_MISMATCH보다 구체적
    RESERVATION_POLICY_EXPIRED(HttpStatus.GONE, "해당 예약 정책이 더 이상 유효하지 않습니다."),
    SYSTEM_TOTAL_DAILY_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "오늘 아파트 전체 방문 예약 허용 횟수가 초과되었습니다."),
    SYSTEM_TOTAL_MONTHLY_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "이번 달 아파트 전체 방문 예약 허용 횟수가 초과되었습니다."),
    SYSTEM_RESERVATION_DISABLED(HttpStatus.FORBIDDEN, "현재 시스템 설정에 의해 방문 예약 서비스가 중단되었습니다."),
    RESERVATION_NOT_TODAY(HttpStatus.BAD_REQUEST, "방문 예약은 최소 방문일 하루 전까지 신청 가능합니다."),
    CANCEL_NOT_TODAY(HttpStatus.BAD_REQUEST, "방문 당일에는 예약을 취소할 수 없습니다."),
    CANNOT_EDIT_RESERVATION(HttpStatus.BAD_REQUEST, "이미 승인되었거나 처리 중인 예약은 수정할 수 없습니다."),
    RESERVATION_CANNOT_EDIT_STATUS(HttpStatus.BAD_REQUEST, "대기(PENDING) 상태인 예약만 수정이 가능합니다."),
    RESERVATION_NOT_OWNER_EDIT(HttpStatus.FORBIDDEN, "본인의 예약만 수정할 수 있습니다."),


    //AI
    AI_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR,"서비스 호출에 실패하였습니다."),
    REDIS_CONNECTION_FAILURE(HttpStatus.SERVICE_UNAVAILABLE,"실시간 서비스 이용이 불가능합니다."),
    EXTERNAL_API_ERROR(HttpStatus.BAD_GATEWAY, "외부 결제 시스템과의 통신 중 오류가 발생했습니다."),
    PG_PROVIDER_ERROR(HttpStatus.BAD_GATEWAY, "결제 서비스 호출에 실패하였습니다."),

    // POINT 관련
    POINT_NOT_ENOUGH(HttpStatus.BAD_REQUEST, "포인트가 부족합니다."),
    POINT_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 포인트 내역을 찾을 수 없습니다."),
    POINT_EXPIRED(HttpStatus.BAD_REQUEST, "포인트가 만료되었습니다."),
    POINT_ALREADY_USED(HttpStatus.BAD_REQUEST, "이미 사용된 포인트입니다."),
    POINT_INVALID_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 포인트 요청입니다."),

    // SYSTEM SETTING
    SYSTEM_SETTING_NOT_FOUND(HttpStatus.NOT_FOUND, "필요한 시스템 설정값을 찾을 수 없습니다."),

    // [추가] 정의되지 않은 모든 서버 에러를 위한 공통 코드
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다. 관리자에게 문의하세요."),

    //Admin
    ADMIN_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 관리자 정보를 찾을 수 없습니다"),

    POLICY_NOT_MODIFIABLE(HttpStatus.NOT_MODIFIED,"만료 예정 정책은 수정할 수 없습니다.");



    private final HttpStatus status;
    private final String message;
}
