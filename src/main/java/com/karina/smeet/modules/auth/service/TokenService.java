package com.karina.smeet.modules.auth.service;

import com.karina.smeet.entity.postgre.RefreshToken;
import com.karina.smeet.entity.postgre.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.UUID;

public interface TokenService {
    RefreshToken createRefreshToken(User user, HttpServletResponse response);

    RefreshToken validateRefreshToken(HttpServletRequest request);

    void deleteRefreshToken(HttpServletRequest request, HttpServletResponse response);

    //delete all token of user(logout on all devices)
    void deleteAllRefreshTokens(UUID userId, HttpServletResponse response);
}

