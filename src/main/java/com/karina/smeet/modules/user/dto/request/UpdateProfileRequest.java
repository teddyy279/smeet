package com.karina.smeet.modules.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import static io.lettuce.core.pubsub.PubSubOutput.Type.message;

public record UpdateProfileRequest(
        @NotBlank(message = "DISPLAY_NAME_BLANK")
        @Size(max = 20, message = "DISPLAY_NAME_TOO_LONG")
        String displayName
) {}
