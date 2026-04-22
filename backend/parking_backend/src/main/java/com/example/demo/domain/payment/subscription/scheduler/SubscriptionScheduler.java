package com.example.demo.domain.payment.subscription.scheduler;

import com.example.demo.domain.payment.subscription.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 정기권 상태를 자동으로 관리하는 스케줄러.
 * 서버가 실행 중인 동안 지정된 주기마다 자동으로 실행됩니다.
 * @Scheduled : 스프링이 주기적으로 메서드를 자동 호출해주는 어노테이션
 * cron 표현식 형식 : "초 분 시 일 월 요일"
 */
@Slf4j
@Component  // 스프링 빈으로 등록 — @Service가 아닌 @Component를 쓰는 것이 스케줄러의 관례
@RequiredArgsConstructor
public class SubscriptionScheduler {

    private final SubscriptionRepository subscriptionRepository;

    /**
     * [매일 자정 실행] 만료된 정기권 일괄 EXPIRED 처리
     *
     * 실행 주기 : cron = "0 0 0 * * *"
     *   → 0초 0분 0시 (자정), 매일, 매월, 요일 무관
     *
     * 처리 대상 : status = ACTIVE 이면서 endDate < 현재 시각인 정기권
     * 처리 결과 : status = EXPIRED 로 일괄 UPDATE
     *
     * @Transactional 필수 — @Modifying(UPDATE 쿼리)은 트랜잭션 안에서만 실행 가능
     */
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void expireSubscriptions() {
        LocalDateTime now = LocalDateTime.now();
        int count = subscriptionRepository.expireSubscriptions(now);
        log.info("[정기권 만료 처리 완료] 처리 건수: {}건", count);
    }
}
