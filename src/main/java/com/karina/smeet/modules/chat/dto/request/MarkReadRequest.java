package com.karina.smeet.modules.chat.dto.request;

import jakarta.validation.constraints.NotBlank;

public record MarkReadRequest(
        @NotBlank(message = "Room id cannot be empty")
        String roomId
) {}
