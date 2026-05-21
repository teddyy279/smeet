package com.karina.smeet.modules.auth.dto.response;

import lombok.Builder;

import java.util.UUID;

@Builder
public record AuthResponse(
    UUID userId,
    String username,
    String email,
    String displayName,
    String avatarUrl,

    String accessToken,
    long accessTokenExpiresIn
) {}
