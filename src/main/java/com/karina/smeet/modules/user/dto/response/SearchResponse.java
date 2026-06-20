package com.karina.smeet.modules.user.dto.response;

import lombok.Builder;
import java.util.List;
import java.util.UUID;

@Builder
public record SearchResponse(
        List<FriendItem> friends,
        List<RoomItem> rooms
) {

    @Builder
    public record FriendItem(
            UUID userId,
            String username,
            String displayName,
            String avatarUrl,
            boolean isOnline,
            UUID directRoomId
    ) {}

    @Builder
    public record RoomItem(
            UUID roomId,
            String name,
            String avatarUrl,
            int memberCount
    ) {}
}