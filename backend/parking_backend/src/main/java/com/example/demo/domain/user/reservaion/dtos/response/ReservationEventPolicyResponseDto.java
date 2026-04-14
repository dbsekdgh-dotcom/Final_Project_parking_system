package com.example.demo.domain.user.reservaion.dtos.response;


import lombok.Getter;

/**
 * 방문 예약 정책 및 사용자의 잔여 상태를 보여주기 위한 불변(Immutable) DTO
 */
@Getter
public class ReservationEventPolicyResponseDto {

    // 1. 정책 정보 (Final)
    private final String eventName;             // 정책명: "기본 방문 예약 정책"
    private final int permittedMinutes;         // 허용 시간: 120 (분)

    // 2. 제한 수치 (Final)
    private final int dailyLimit;               // 일일 제한: 5
    private final int monthlyLimit;             // 월간 제한: 100
    private final int maxActiveReservations;    // 동시 보유 가능 수: 3

    // 3. 사용자 이용 현황 (Final)
    private final long todayUsedCount;          // 오늘 사용한 횟수
    private final long monthUsedCount;          // 이번 달 사용한 횟수
    private final int currentActiveCount;       // 현재 보유 중인 예약 수

    // 4. 패널티 정보 (Final)
    private final boolean noShowPenaltyEnabled; // 패널티 활성화 여부
    private final String penaltyMessage;        // 안내 문구

    // 직접 작성하는 생성자
    public ReservationEventPolicyResponseDto(
            String eventName,
            int permittedMinutes,
            int dailyLimit,
            int monthlyLimit,
            int maxActiveReservations,
            long todayUsedCount,
            long monthUsedCount,
            int currentActiveCount,
            boolean noShowPenaltyEnabled,
            String penaltyMessage) {
        this.eventName = eventName;
        this.permittedMinutes = permittedMinutes;
        this.dailyLimit = dailyLimit;
        this.monthlyLimit = monthlyLimit;
        this.maxActiveReservations = maxActiveReservations;
        this.todayUsedCount = todayUsedCount;
        this.monthUsedCount = monthUsedCount;
        this.currentActiveCount = currentActiveCount;
        this.noShowPenaltyEnabled = noShowPenaltyEnabled;
        this.penaltyMessage = penaltyMessage;
    }
}
