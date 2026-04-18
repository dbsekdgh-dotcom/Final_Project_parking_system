package com.example.demo.domain.auth.user.service;

import com.example.demo.global.exception.AuthException;
import com.example.demo.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserVerificationService {

    private final StringRedisTemplate redisTemplate;

    private static final String PREFIX = "AUTH_CODE:";
    private static final String PASS_PREFIX = "AUTH_PASS:";
    private static final String RT_PREFIX = "RT:";

    private static final long LIMIT_TIME = 3 * 60;
    private static final long PASS_LIMIT_TIME = 5 * 60;
    private static final long RT_LIMIT_TIME = 6 * 60 * 60;

    private static final String LOGIN_FAIL_PREFIX = "LOGIN_FAIL:";
    private static final int MAX_FAIL_COUNT = 5;
    private static final int BLOCK_TIME_MINUTES = 5;


    public void saveCode(String email, String code) {
        redisTemplate.opsForValue().set(PREFIX + email, code, Duration.ofSeconds(LIMIT_TIME));
    }

    /**
     * 인증번호 검증 (성공 시 void, 실패 시 Exception)
     */
    public void verifyCode(String email, String userInputCode) {
        String redisKey = PREFIX + email;
        String savedCode = redisTemplate.opsForValue().get(redisKey);

        // 1. Redis에 데이터가 없는 경우 (시간 만료 -2 상황)
        if (savedCode == null) {
            log.warn("### [인증 실패] 시간 만료: {}", email);
            throw new AuthException(ErrorCode.VERIFICATION_CODE_EXPIRED);
        }

        // 2. 번호가 틀린 경우
        if (!savedCode.equals(userInputCode)) {
            log.warn("### [인증 실패] 번호 불일치: {}", email);
            throw new AuthException(ErrorCode.VERIFICATION_CODE_MISMATCH);
        }

        // 3. 인증 성공 시 Redis에서 삭제
        redisTemplate.delete(redisKey);

        this.deleteLoginFailCount(email);

        log.info("### [인증 성공] 이메일: {} (로그인 차단 해제 완료)", email);

    }

    public void saveVerificationPass(String email) {
        redisTemplate.opsForValue().set(PASS_PREFIX + email, "true", Duration.ofSeconds(PASS_LIMIT_TIME));
    }

    public boolean hasVerificationPass(String email) {
        String pass = redisTemplate.opsForValue().get(PASS_PREFIX + email);
        return "true".equals(pass);
    }

    public void deleteCode(String email) {
        redisTemplate.delete(PREFIX + email);
    }

    public void deleteVerificationPass(String email) {
        redisTemplate.delete(PASS_PREFIX + email);
    }

    public void saveRefreshToken(String email, String refreshToken) {
        redisTemplate.opsForValue().set(RT_PREFIX + email, refreshToken, Duration.ofSeconds(RT_LIMIT_TIME));
    }

    public String getRefreshToken(String email) {
        return redisTemplate.opsForValue().get(RT_PREFIX + email);
    }

    public void deleteRefreshToken(String email) {
        redisTemplate.delete(RT_PREFIX + email);
    }

    public void increaseLoginFailCount(String email) {
        String key = LOGIN_FAIL_PREFIX + email;

        Long count = redisTemplate.opsForValue().increment(key);

        redisTemplate.expire(key, BLOCK_TIME_MINUTES, TimeUnit.MINUTES);

        log.info("[보안] {} 계정 로그인 실패 횟수: {}", email, count);
    }

    public int getLoginFailCount(String email) {
        String key = LOGIN_FAIL_PREFIX + email;
        String value = redisTemplate.opsForValue().get(key);
        return (value == null) ? 0 : Integer.parseInt(value);
    }

    public void deleteLoginFailCount(String email) {
        String key = LOGIN_FAIL_PREFIX + email;
        redisTemplate.delete(key);
        log.info(" [보안] {} 계정의 로그인 실패 기록이 초기화되었습니다.", email);
    }

}