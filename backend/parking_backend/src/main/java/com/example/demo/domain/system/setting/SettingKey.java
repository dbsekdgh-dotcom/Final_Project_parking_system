package com.example.demo.domain.system.setting;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SettingKey {
    // 정산 후 출차 가능 시간 (분) - 기본값 5분
    POST_PAYMENT_GRACE_MINUTES("POST_PAYMENT_GRACE_MINUTES", "5"),

    // 정산 유효 시간 (분) - 기본값 5분
    PAYMENT_VALID_MINUTES("PAYMENT_VALID_MINUTES", "5"),

    // 트랜잭션 LOCK용 행
    ENTRY_ROCK("ENTRY_ROCK", "mutex"),

    // DETECTED 상태 자동 취소 시간 (분) - 기본값 1분
    DETECTED_CANCEL_MINUTES("DETECTED_CANCEL_MINUTES", "1"),

    // 최소 사용 포인트
    MIN_USAGE_POINT("MIN_USAGE_POINT","100"),

    // 적립율 설정
    PAYMENT_POINT_EARN_RATE("PAYMENT_POINT_EARN_RATE","5"),

    // 주차장 전체 세대 일일 총 방문 예약 가능 횟수 - 기본값 10회
    TOTAL_DAILY_RESERVATION_LIMIT("TOTAL_DAILY_RESERVATION_LIMIT", "10"),

    // 차량 자동 승인 유사도 기준치 (%) - 기본값 95%
    VEHICLE_AUTO_APPROVAL_THRESHOLD("VEHICLE_AUTO_APPROVAL_THRESHOLD", "95"),

    //사전정산 후 출차시간 초과 시 최소 부과 요금
    OVERTIME_MIN_FEE("OVERTIME_MIN_FEE","100"),

    //블랙리스트 등록을 위한 신고 횟수 기준
    REPORT_BLACKLIST_THRESHOLD("REPORT_BLACKLIST_THRESHOLD","10"),

    // 정기권 월별 최대 판매 수량 (선착순)
    SUB_MAX_COUNT("SUB_MAX_COUNT", "10"),

    // 정기권 30일권 이용 금액
    SUB_MONTHLY_PRICE("SUB_MONTHLY_PRICE", "100000"),

    // 정기권 기본 이용 기간 (일)
    SUB_DURATION_DAYS("SUB_DURATION_DAYS", "30");


    private final String key;
    private final String defaultValue;

    public int getDefaultIntValue(){
        return Integer.parseInt(this.defaultValue);
    }

}
