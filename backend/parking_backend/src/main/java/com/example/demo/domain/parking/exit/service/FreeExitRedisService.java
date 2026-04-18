package com.example.demo.domain.parking.exit.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class FreeExitRedisService {

    private final StringRedisTemplate redisTemplate;
    private static final String KET_PREFIX = "freeExit";

    public void register(Long parkingLogId, LocalDateTime freeExitUntil){
        if (freeExitUntil == null) return;
        Duration ttl = Duration.between(LocalDateTime.now(),freeExitUntil);
        if (ttl.isNegative() || ttl.isZero()) return;
        if (ttl.toDays() > 365) return;
        redisTemplate.opsForValue().set(KET_PREFIX + parkingLogId, String.valueOf(parkingLogId), ttl);
    }

    public void delete(Long parkingLogId){
        redisTemplate.delete(KET_PREFIX+parkingLogId);
    }
}
