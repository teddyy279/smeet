package com.karina.smeet.modules.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    // Batched EXISTS via a single Redis pipeline instead of one round-trip per user.
    public Map<UUID, Boolean> isOnline(Collection<UUID> userIds) {
        if (userIds.isEmpty()) return Map.of();

        List<UUID> ids = List.copyOf(userIds);
        List<Object> results = redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            for (UUID id : ids) {
                connection.exists(KEY.formatted(id).getBytes(StandardCharsets.UTF_8));
            }
            return null;
        });

        Map<UUID, Boolean> statuses = new HashMap<>();
        for (int i = 0; i < ids.size(); i++) {
            statuses.put(ids.get(i), Boolean.TRUE.equals(results.get(i)));
        }
        return statuses;
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
