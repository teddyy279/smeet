package com.karina.smeet.modules.room.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AddMemberRequest(
        @NotNull(message = "User id cannot be null")
        UUID userId
) {}
