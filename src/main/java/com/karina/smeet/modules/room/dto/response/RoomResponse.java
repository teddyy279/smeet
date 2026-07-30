package com.karina.smeet.modules.room.dto.response;


import com.karina.smeet.entity.postgre.Room;
import com.karina.smeet.entity.postgre.Roommember;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder

public record RoomResponse(
        UUID id,
        Room.Type type,
        String name,
        String avatarUrl,

        int memberCount,
        Instant createdAt,

        Roommember.Role myRole,

        String lastMessagePreview,
        Instant lastMessageAt,

        long unreadCount
) {}
