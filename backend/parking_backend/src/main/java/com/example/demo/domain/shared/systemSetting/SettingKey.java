package com.example.demo.domain.shared.systemSetting;

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
    DETECTED_CANCEL_MINUTES("DETECTED_CANCEL_MINUTES", "1");

    private final String key;
    private final String defaultValue;

    public int getDefaultIntValue(){
        return Integer.parseInt(this.defaultValue);
    }

}
