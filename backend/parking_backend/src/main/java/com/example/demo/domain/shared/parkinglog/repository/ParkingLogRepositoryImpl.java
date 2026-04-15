package com.example.demo.domain.shared.parkinglog.repository;

import com.example.demo.domain.shared.parkinglog
        .QParkingLog;
import com.example.demo.domain.shared.parkinglog.dtos.response.ParkingLogSummaryResponse;
import com.example.demo.domain.shared.parkinglog.enums.ParkingStatus;
import com.example.demo.domain.shared.parkinglog.enums.PaymentStatus;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@RequiredArgsConstructor
public class ParkingLogRepositoryImpl implements ParkingLogRepositoryCustom{
    private final JPAQueryFactory queryFactory;
    private static final QParkingLog parkingLog = QParkingLog.parkingLog;

    @Override
    public ParkingLogSummaryResponse getParkingSummary(){
        //오늘 날짜의 시작과 끝 계산
        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
        LocalDateTime endOfToday = LocalDate.now().atTime(LocalTime.MAX);

        //상단 카드용 4가지 집계 데이터를 한번에 조회
        //현재 주차 대수(입차는 했으나 아직 나가지 않은 차량)
        long currentParkingCount = queryFactory
                .select(parkingLog.count())
                .from(parkingLog)
                .where(parkingLog.parkingStatus.eq(ParkingStatus.ENTERED))
                .fetchOne();

        //출차 완료 대수(오늘 날짜에 출차 완료된 차량)
        long todayExitedCount = queryFactory
                .select(parkingLog.count())
                .from(parkingLog)
                .where(parkingLog.parkingStatus.in(ParkingStatus.EXITED,ParkingStatus.FORCE_EXITED)
                        .and(parkingLog.exitedAt.between(startOfToday, endOfToday)))
                .fetchOne();

        //미납건수 (결제 상태가 UNPAID인 모든 차량)
        long unpaidCount = queryFactory
                .select(parkingLog.count())
                .from(parkingLog)
                .where(parkingLog.paymentStatus.eq(PaymentStatus.UNPAID))
                .fetchOne();

        //금일 로그 (오늘 입차 감지된 모든 차량 건수)
        long todayLogCount = queryFactory
                .select(parkingLog.count())
                .from(parkingLog)
                .where(parkingLog.entryTime.between(startOfToday,endOfToday))
                .fetchOne();

        //DTO로 변환해 반환
        return ParkingLogSummaryResponse.builder()
                .currentParkingCount(currentParkingCount)
                .todayExitedCount(todayExitedCount)
                .unpaidCount(unpaidCount)
                .todayLogCount(todayLogCount)
                .build();
    }

}
