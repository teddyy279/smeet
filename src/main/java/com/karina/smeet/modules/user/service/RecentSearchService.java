package com.karina.smeet.modules.user.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.karina.smeet.entity.postgre.Room;
import com.karina.smeet.entity.postgre.User;
import com.karina.smeet.modules.user.repository.UserRepository;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j

public class RecentSearchService {
    StringRedisTemplate redisTemplate;
    UserRepository userRepository;
    ObjectMapper objectMapper;

    private static final String KEY = "recent_search:%s";
    private static final int MAX_ITEMS = 10;
    private static final long TTL_DAYS = 30;

    public enum TargetType { USER, ROOM }

    @Data
    @Builder
    public static class RecentSearchItem {
        private String targetId;
        private TargetType type; //ROOM, USER
        private String name;//displayName
        private String avatarUrl;
        private String username; // Only user have username
    }

    public void saveUser(UUID currentUserId, User target) {
        RecentSearchItem item = RecentSearchItem.builder()
                .targetId(target.getId().toString())
                .type(TargetType.USER)
                .name(target.getDisplayName())
                .username(target.getUsername())
                .avatarUrl(target.getAvatarUrl())
                .build();

        save(currentUserId, item);
    }

    public void saveRoom(UUID currentUserId, Room target) {
        RecentSearchItem item = RecentSearchItem.builder()
                .targetId(target.getId().toString())
                .type(TargetType.ROOM)
                .name(target.getName())
                .avatarUrl(target.getAvatarUrl())
                .build();

        save(currentUserId, item);
    }

    public List<RecentSearchItem> getRecent(UUID currentUserId) {
        String key = KEY.formatted(currentUserId);

        Set<String> members = redisTemplate.opsForZSet()
                .reverseRange(key, 0, -1);

        if(members == null) return List.of();

        return members.stream()
                .map(this::deserialize)
                .toList();
    }

    public void remove(UUID currentUserId, String targetId) {
        String key = KEY.formatted(currentUserId);
        Set<String> members = redisTemplate.opsForZSet().range(key, 0, -1);
        if (members == null) return;

        members.stream()
                .filter(m -> {
                    RecentSearchItem item = deserialize(m);
                    return item != null && item.getTargetId().equals(targetId);
                })
                .findFirst()
                .ifPresent(m -> redisTemplate.opsForZSet().remove(key, m));

    }

    public void clear(UUID currentUserId) {
        redisTemplate.delete(KEY.formatted(currentUserId));
    }

    private void save(UUID currentUserId, RecentSearchItem item) {
        String key = KEY.formatted(currentUserId);
        String member = serialize(item);
        double score = Instant.now().toEpochMilli();

        remove(currentUserId, item.getTargetId()); // delete old item if same targetId

        redisTemplate.opsForZSet().add(key, member, score);

        long size = redisTemplate.opsForZSet().zCard(key);
        if (size > MAX_ITEMS) {
            redisTemplate.opsForZSet().removeRange(key, 0, size - MAX_ITEMS - 1);
        }
        redisTemplate.expire(key, Duration.ofDays(TTL_DAYS));
    }

    private String serialize(RecentSearchItem item) {
        try {
            return objectMapper.writeValueAsString(item);
        } catch (JsonProcessingException exception) {
            log.error("Failed to serialize RecentSearch", exception);
            return "";
        }
    }

    private RecentSearchItem deserialize(String json) {
        try {
            return objectMapper.readValue(json, RecentSearchItem.class);
        } catch (JsonProcessingException exception) {
            log.error("Failed to deserialize RecentSearchItem", exception);
            return null;
        }
    }

}
