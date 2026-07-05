package com.karina.smeet.modules.friend.dto.response;

import com.karina.smeet.entity.postgre.Friendship;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
public record FriendRequestResponse(
    UUID friendshipId,
    UUID requesterId,
    String requesterUsername,
    String requesterDisplayname,
    String requesterAvatarUrl,
    Friendship.Status status,
    Instant createdAt
) {}
