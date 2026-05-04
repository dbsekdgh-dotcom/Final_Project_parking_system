package com.example.demo.domain.reservation.dtos.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

/**
 * 방문 예약 신청 모달에서 정책 안내 및 실시간 잔여 횟수를 보여주기 위한 DTO
 */
@Getter
@Schema(description = "예약 정책 및 날짜별 잔여 현황 응답 — 단지 공통 정책(일/월 한도, 허용 시간 등)과 " +
        "사용자의 누적 사용량, 선택한 날짜의 실시간 잔여 슬롯 정보를 함께 반환합니다.")
public class ReservationEventPolicyResponseDto {

    // --- [1. 아파트 공통 고정 정책] ---
    private final String eventName;             // 정책명 (예: "2026 봄 시즌 주차 정책")
    private final int permittedMinutes;           // 1회 예약 시 주차 허용 시간 (분)
    private final Integer dailyLimitPerHousehold; // 세대당 하루에 신청 가능한 최대 횟수 (null = 무제한)
    private final Integer monthlyLimit;           // 세대당 한 달간 신청 가능한 총 횟수 (null = 무제한)
    private final int maxActiveReservations;    // 한 세대가 동시에 보유(대기/승인)할 수 있는 예약증 수
    private final int totalDailyLimit;          // 아파트 전체에서 하루에 받을 수 있는 총 예약 대수 (예: 10대)

    // --- [2. 사용자의 현재 누적 상태 (잔여 횟수 계산용)] ---
    private final long monthUsedCount;          // 이번 달에 이미 사용한 총 횟수
    private final int currentActiveCount;       // 현재 취소되지 않고 활성화된 예약증 수 (승인 대기 포함)

    // --- [3. 선택한 특정 날짜(Target Date)의 실시간 현황] ---
    private final long targetDateTotalCount;    // 사용자가 선택한 날짜에 아파트 전체 예약이 몇 건 차 있는지
    private final long targetDateUserCount;     // 사용자가 선택한 날짜에 본인이 이미 몇 건 예약했는지

    // --- [4. 안내 및 시스템 메시지] ---
    private final boolean noShowPenaltyEnabled; // 노쇼 페널티 적용 여부
    private final String systemMessage;         // "해당 날짜 예약 마감" 등 예약 불가능 시 띄울 메시지
    private final String warningMessage;        // "공간 미보장 안내" 등 하단에 상시 노출할 주의 문구

    public ReservationEventPolicyResponseDto(
            String eventName,
            int permittedMinutes,
            Integer dailyLimitPerHousehold,
            Integer monthlyLimit,
            int maxActiveReservations,
            int totalDailyLimit,
            long monthUsedCount,
            int currentActiveCount,
            long targetDateTotalCount,
            long targetDateUserCount,
            boolean noShowPenaltyEnabled,
            String systemMessage,
            String warningMessage) {
        this.eventName = eventName;
        this.permittedMinutes = permittedMinutes;
        this.dailyLimitPerHousehold = dailyLimitPerHousehold;
        this.monthlyLimit = monthlyLimit;
        this.maxActiveReservations = maxActiveReservations;
        this.totalDailyLimit = totalDailyLimit;
        this.monthUsedCount = monthUsedCount;
        this.currentActiveCount = currentActiveCount;
        this.targetDateTotalCount = targetDateTotalCount;
        this.targetDateUserCount = targetDateUserCount;
        this.noShowPenaltyEnabled = noShowPenaltyEnabled;
        this.systemMessage = systemMessage;
        this.warningMessage = warningMessage;
    }
}