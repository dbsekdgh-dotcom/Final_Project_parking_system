package com.example.demo.domain.parking.log.dtos.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ParkingLogSummaryResponse { // 관리자 주차관리 페이지 - 상단 요약정보 Dto
    private long currentParkingCount; // 현재 주차 (STATUS: ENTERED)
    private long todayExitedCount; // 금일 출차 완료 (STATUS: EXITED, FORCE_EXITED & 오늘 날짜)
    private long unpaidCount; // 현재 미납 총건수 (PAYMENT: UNPAID)
    private long todayLogCount; // 금일 전체 발생 로그 (입/출차 등 전체 흐름)
}
