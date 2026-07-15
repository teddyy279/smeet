package com.karina.smeet.modules.room.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record CreateRoomRequest(
        @NotBlank(message = "Room name cannot be empty")
        @Size(max = 50, message = "Room name must not exceed 100 characters")
        String name,

        String avatarUrl,

        @NotEmpty(message = "Room must have at least 1 initial member")
        List<UUID> memberIds
) {}
