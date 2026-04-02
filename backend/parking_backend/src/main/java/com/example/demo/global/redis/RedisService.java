package com.example.demo.global.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisService {
    private final StringRedisTemplate redisTemplate;

    /**
     * Refresh Token 저장
     * @param loginId  사용자 ID (Key의 식별자)
     * @param refreshToken 저장할 토큰값
     * @param durationMinutes 유지 시간 (분 단위)
     */
    public void saveRefreshToken(String loginId,String refreshToken,long durationMinutes){
        redisTemplate.opsForValue().set(
                "RT:"+loginId,
                refreshToken,
                durationMinutes,
                TimeUnit.MINUTES
        );
    }

    //토큰 조회(검증시 사용)
    public String getRefreshToken(String loginId){
        return redisTemplate.opsForValue().get("RT:"+loginId);
    }

    //토큰 삭제(로그아웃시 사용)
    public void deleteRefreshToken(String loginId){
        redisTemplate.delete("RT:"+loginId);
    }
}
