package com.example.demo.domain.user.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class UserVerificationService {

    private final StringRedisTemplate redisTemplate;
    private static final String PREFIX = "AUTH_CODE:";
    private static final String PASS_PREFIX = "AUTH_PASS:";
    private static final long LIMIT_TIME = 3* 60;
    private static final long PASS_LIMIT_TIME = 5 * 60;

    public void saveCode(String email, String code) {
        redisTemplate.opsForValue().set(PREFIX + email, code, Duration.ofSeconds(LIMIT_TIME));

    }
    public boolean verifyCode(String email, String code) {
        String savedCode = redisTemplate.opsForValue().get(PREFIX+email);
        return code != null && code.equals(savedCode);
    }

    public void saveVerificationPass(String email) {
        redisTemplate.opsForValue().set(PASS_PREFIX+email,"true", Duration.ofSeconds(PASS_LIMIT_TIME));
    }

    public boolean hasVerificationPass(String email) {
        String pass = redisTemplate.opsForValue().get(PASS_PREFIX+email);
        return "true".equals(pass);
    }

    public void deleteCode(String email) {
        redisTemplate.delete(PREFIX+email);
    }

    public void deleteVerificationPass(String email) {
        redisTemplate.delete(PASS_PREFIX+email);
    }
}
