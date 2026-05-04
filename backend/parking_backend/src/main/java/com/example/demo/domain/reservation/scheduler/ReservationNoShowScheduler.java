package com.example.demo.domain.reservation.scheduler;

import com.example.demo.domain.reservation.repository.ReservationRepository;
import com.example.demo.domain.resident.household.repository.HouseholdRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationNoShowScheduler {

    private final ReservationRepository reservationRepository;
    private final HouseholdRepository householdRepository;

    // 매시간 정각 실행 — visitEndAt 지난 RESERVED/PENDING 예약을 NO_SHOW 처리
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void markNoShow() {
        LocalDateTime now = LocalDateTime.now();

        List<Long> householdIds = reservationRepository.findNoShowTargetHouseholdIds(now);

        int count = reservationRepository.bulkMarkNoShow(now);

        for (Long householdId : householdIds) {
            householdRepository.decrementActiveReservationCount(householdId);
        }

        if (count > 0) {
            log.info("[NoShow 배치] {}건 NO_SHOW 처리, {}개 세대 activeReservationCount 감소", count, householdIds.size());
        }
    }
}
