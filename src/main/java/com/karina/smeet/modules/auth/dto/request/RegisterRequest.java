package com.karina.smeet.modules.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
    @NotBlank(message = "Username cannot be empty")
    @Size(min = 3, message = "Username minimum 3 characters")
    String username,

    @NotBlank(message = "Email cannot be empty")
    @Email(message = "Invalid email")
    String email,

    @NotBlank(message = "Password cannot be empty")
    @Size(min = 6, message = "Password minimum 6 characters")
    String password,

    @NotBlank(message = "Display name cannot be empty")
    @Size(min = 30, message = "Display name minimum 20 characters")
    String displayName
) {}
