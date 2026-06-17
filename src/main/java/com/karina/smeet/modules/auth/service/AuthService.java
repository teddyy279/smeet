package com.karina.smeet.modules.auth.service;

import com.karina.smeet.modules.auth.dto.request.LoginRequest;
import com.karina.smeet.modules.auth.dto.request.RegisterRequest;
import com.karina.smeet.modules.auth.dto.response.AuthResponse;
import com.karina.smeet.modules.auth.dto.response.RefreshTokenResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.UUID;

public interface AuthService {
    AuthResponse register(RegisterRequest request, HttpServletResponse response);

    AuthResponse login(LoginRequest request, HttpServletResponse response);

    RefreshTokenResponse refreshToken(HttpServletRequest request);

    void logout(HttpServletRequest request, HttpServletResponse response);

    void logoutAll(UUID userId, HttpServletResponse response);

    AuthResponse outboundAuthenticate(String code, HttpServletResponse response);
}
