package com.example.demo.domain.reservation.scheduler;

import com.example.demo.domain.reservation.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationNoShowScheduler {

    private final ReservationRepository reservationRepository;

    // 매시간 정각 실행 — visitEndAt 지난 RESERVED/PENDING 예약을 NO_SHOW 처리
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void markNoShow() {
        int count = reservationRepository.bulkMarkNoShow(LocalDateTime.now());
        if (count > 0) {
            log.info("[NoShow 배치] {}건 NO_SHOW 처리 완료", count);
        }
    }
}
