package com.karina.smeet.modules.room.dto.request;

import com.karina.smeet.entity.postgre.Roommember;
import jakarta.validation.constraints.NotNull;

public record UpdateRoleRequest(
        @NotNull(message = "Role cannot be null")
        Roommember.Role role
) {}
