package com.karina.smeet.modules.chat.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ReactionRequest(
        @NotBlank(message = "Message id cannot be empty")
        String messageId,

        @NotBlank(message = "Emoji cannot be empty")
        String emoji
) {}
