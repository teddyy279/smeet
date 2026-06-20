package com.karina.smeet.modules.user.dto.response;

import java.time.Instant;
import java.util.UUID;

public record MyProfileResponse(
    UUID id,
    String username,
    String displayName,
    String avatarUrl,
    Instant createdAt
) {}
