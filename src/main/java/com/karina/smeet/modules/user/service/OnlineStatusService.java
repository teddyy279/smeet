package com.karina.smeet.modules.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor

public class OnlineStatusService {
    private final RedisTemplate redisTemplate;
    private static final String KEY = "user:online:%s";
    private static final long TTL_SEC = 30;

    public boolean isOnline(UUID userId) {
        return Boolean.TRUE.equals(
                redisTemplate.hasKey(KEY.formatted(userId)));
    }

    public void setOnline(UUID userId) {
        redisTemplate.opsForValue().set(
            KEY.formatted(userId), "1",
            Duration.ofSeconds(TTL_SEC));
    }

    public void setOffline(UUID userId) {
        redisTemplate.delete(KEY.formatted(userId));
    }
}
