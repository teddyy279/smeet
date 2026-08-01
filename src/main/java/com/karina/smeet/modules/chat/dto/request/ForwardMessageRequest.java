package com.karina.smeet.modules.chat.dto.request;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record ForwardMessageRequest(
        @NotEmpty(message = "At least one target room is required")
        List<String> targetRoomIds
) {}
