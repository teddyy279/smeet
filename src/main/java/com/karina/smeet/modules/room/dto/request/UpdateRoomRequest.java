package com.karina.smeet.modules.room.dto.request;

import jakarta.validation.constraints.Size;

public record UpdateRoomRequest(
        @Size(max = 50, message = "Room name must not exceed 50 characters")
        String name,

        String avatarUrl
) {}
