package com.example.demo.domain.payment.ticket.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class FreeTicketRedisService {

    private static final String KEY_PREFIX = "store:free-ticket:";
    private static final Duration TTL_30_DAYS = Duration.ofDays(30);

    private final StringRedisTemplate redisTemplate;

    public void register(Long storeId) {
        redisTemplate.opsForValue().set(KEY_PREFIX + storeId, "1", TTL_30_DAYS);
    }

    public void register(Long storeId, Duration ttl) {
        if (ttl.isNegative() || ttl.isZero()) return;
        redisTemplate.opsForValue().set(KEY_PREFIX + storeId, "1", ttl);
    }

    public boolean exists(Long storeId) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(KEY_PREFIX + storeId));
    }

    public void delete(Long storeId) {
        redisTemplate.delete(KEY_PREFIX + storeId);
    }
}
