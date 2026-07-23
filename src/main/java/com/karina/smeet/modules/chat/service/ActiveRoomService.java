package com.karina.smeet.modules.chat.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor

public class ActiveRoomService {
    private final StringRedisTemplate redisTemplate;
    private static final String KEY = "user:active_room:%s";

    public void enterRoom(UUID userId, String roomId) {
        redisTemplate.opsForValue().set(KEY.formatted(userId), roomId);
    }

    public void leaveRoom(UUID userId) {
        redisTemplate.delete(KEY.formatted(userId));
    }

    public boolean isViewingRoom(UUID userId, String roomId) {
        String activeRoom = redisTemplate.opsForValue().get(KEY.formatted(userId));
        return roomId.equals(activeRoom);
    }
}
