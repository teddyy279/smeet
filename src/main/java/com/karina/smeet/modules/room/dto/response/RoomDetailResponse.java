package com.karina.smeet.modules.room.dto.response;

import com.karina.smeet.entity.postgre.Room;
import com.karina.smeet.entity.postgre.Roommember;
import lombok.Builder;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Builder
public record RoomDetailResponse(
        UUID id,
        Room.Type type,
        String name,
        String avatarUrl,
        Instant createdAt,
        Roommember.Role myRole,
        List<RoomMemberResponse> members
) {
    @Builder
    public record RoomMemberResponse(
            UUID userId,
            String username,
            String displayName,
            String avatarUrl,
            boolean isOnline,
            Roommember.Role role,
            Instant joinedAt
    ) {}
}