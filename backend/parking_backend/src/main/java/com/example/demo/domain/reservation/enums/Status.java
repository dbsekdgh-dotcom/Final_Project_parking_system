package com.example.demo.domain.reservation.enums;

import lombok.Getter;

@Getter
public enum Status {
    PENDING,    // 승인 대기 (신청 직후)
    RESERVED,   // 예약 확정 (관리자 승인 후)
    REJECTED,   // 거절 (관리자가 거절)
    CANCELLED,  // 취소 (사용자가 취소)
    ENTERED,    // 입차 완료
    NO_SHOW,    // 미방문
    COMPLETED   // 출차 완료 (이용 종료)
}