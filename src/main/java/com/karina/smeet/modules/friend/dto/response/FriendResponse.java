package com.karina.smeet.modules.friend.dto.response;

import lombok.Builder;

import java.util.UUID;


@Builder

public record FriendResponse(
    UUID userId,
    String username,
    String displayName,
    String avatarUrl,
    boolean isOnline,
    UUID directRoomId
) {}
