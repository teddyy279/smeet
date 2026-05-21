package com.karina.smeet.modules.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "Email or username cannot be empty")
        @Email(message = "Invalid email")
        String identifier,

        @NotBlank(message = "Password cannot be empty")
        String password
) {}
