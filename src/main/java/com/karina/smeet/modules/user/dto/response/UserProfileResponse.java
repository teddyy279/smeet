package com.karina.smeet.modules.user.dto.response;

import lombok.Builder;

import java.util.UUID;

@Builder

public record UserProfileResponse(
        UUID id,
        String username,
        String displayName,
        String avatarUrl,
        boolean isOnline,
        RelationshipStatus relationshipStatus
) {
    public enum RelationshipStatus {
        FRIEND,
        PENDING_SENT,
        PENDING_RECEIVED,
        NONE
    }
}