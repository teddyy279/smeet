package com.karina.smeet.modules.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
    @NotBlank(message = "CURRENT_PASSWORD_NOT_BLANK")
    String currentPassword,

    @NotBlank(message = "NEW_PASSWORD_NOT_BLANK")
    @Size(min = 6, message = "PASSWORD_TOO_SHORT")
    String newPassword
) {}
