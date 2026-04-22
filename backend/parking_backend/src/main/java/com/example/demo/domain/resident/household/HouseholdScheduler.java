package com.example.demo.domain.resident.household;

import com.example.demo.domain.resident.household.repository.HouseholdRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class HouseholdScheduler {

    private final HouseholdRepository householdRepository;

    // 매일 자정 todayVisitCount 초기화
    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    @Transactional
    public void resetDailyVisitCount() {
        householdRepository.resetTodayVisitCount();
        log.info("[HouseholdScheduler] todayVisitCount 전체 초기화 완료");
    }

    // 매월 1일 자정 monthlyVisitCount 초기화
    @Scheduled(cron = "0 0 0 1 * *", zone = "Asia/Seoul")
    @Transactional
    public void resetMonthlyVisitCount() {
        householdRepository.resetMonthlyVisitCount();
        log.info("[HouseholdScheduler] monthlyVisitCount 전체 초기화 완료");
    }
}
