package com.karina.smeet.modules.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
    @NotBlank String email,
    @NotBlank String otp,
    @Size(min = 8, message = "new password minimum 8 characters")
    String newPassword
) {}
