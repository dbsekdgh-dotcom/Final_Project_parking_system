package com.example.demo.domain.payment.ticket.service;

import com.example.demo.domain.store.StoreTicketConfig;
import com.example.demo.domain.store.repository.StoreTicketConfigRepository;
import com.example.demo.domain.system.store.enums.Status;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class FreeTicketStartupRegistrar implements ApplicationRunner {

    private final StoreTicketConfigRepository configRepository;
    private final FreeTicketRedisService redisService;
    private final FreeTicketProvideService provideService;

    @Override
    public void run(ApplicationArguments args) {
        List<StoreTicketConfig> configs = configRepository.findAllActiveWithQuota(Status.ACTIVE);
        log.info("startup: free-ticket 복구 대상 {}건", configs.size());

        for (StoreTicketConfig config : configs) {
            Long storeId = config.getStore().getStoreId();

            if (redisService.exists(storeId)) continue;

            LocalDateTime lastIssuedAt = config.getLastIssuedAt();
            if (lastIssuedAt == null) {
                // 한 번도 지급 안 된 경우 — 즉시 지급
                log.info("startup: 미지급 상가 즉시 지급, storeId={}", storeId);
                provideService.provideAndSchedule(storeId);
            } else {
                LocalDateTime nextDue = lastIssuedAt.plusDays(30);
                if (!LocalDateTime.now().isBefore(nextDue)) {
                    // 만료일이 지난 경우 — 즉시 지급
                    log.info("startup: 지급 기한 초과 즉시 지급, storeId={}", storeId);
                    provideService.provideAndSchedule(storeId);
                } else {
                    // 아직 만료 전 — 남은 시간으로 Redis 키 복구
                    Duration remaining = Duration.between(LocalDateTime.now(), nextDue);
                    redisService.register(storeId, remaining);
                    log.info("startup: Redis 키 복구, storeId={}, 남은시간={}h", storeId, remaining.toHours());
                }
            }
        }
    }
}
