package com.karina.smeet.modules.auth.service;

import com.karina.smeet.common.exception.AppException;
import com.karina.smeet.common.exception.ErrorCode;
import com.karina.smeet.entity.postgre.RefreshToken;
import com.karina.smeet.entity.postgre.User;
import com.karina.smeet.modules.auth.repository.RefreshTokenRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)

public class TokenServiceImpl implements TokenService{
    final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.refresh-token-expiry}")
    long refreshTokenExpiry;

    @Override
    @Transactional
    public RefreshToken createRefreshToken(User user, HttpServletResponse response) {
        String refreshTokenStr = UUID.randomUUID().toString();

        ResponseCookie cookie = ResponseCookie.from("refresh-token", refreshTokenStr)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(refreshTokenExpiry)
                .sameSite("Lax")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        RefreshToken token = RefreshToken.builder()
                .token(refreshTokenStr)
                .user(user)
                .expiryTime(Instant.now().plusSeconds(refreshTokenExpiry))
                .build();

        return refreshTokenRepository.save(token);
    }


    @Override
    @Transactional
    public RefreshToken validateRefreshToken(HttpServletRequest request) {
        String refreshTokenStr = getCookieValue(request, "refresh-token");

        var refreshToken = refreshTokenRepository.findByToken(refreshTokenStr)
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));

        if(refreshToken.getExpiryTime().isBefore(Instant.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new AppException(ErrorCode.REFRESH_TOKEN_EXPIRED);
        }
        return refreshToken;
    }

    private String getCookieValue(HttpServletRequest httpServletRequest, String name) {
        if (httpServletRequest == null) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        var cookies = httpServletRequest.getCookies();

        if (cookies == null) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        for (var cookie : cookies) {
            if (cookie.getName().equals(name)) {
                return cookie.getValue();
            }
        }

        throw new AppException(ErrorCode.UNAUTHENTICATED);
    }

    @Override
    @Transactional
    public void deleteRefreshToken(HttpServletRequest request, HttpServletResponse response) {
        try {
            String refreshTokenStr = getCookieValue(request, "refresh-token");
            refreshTokenRepository.deleteByToken(refreshTokenStr);
        } catch (AppException exception) {
            log.info("No Refresh Token found in cookie or already deleted");
        }
        ResponseCookie cookie = ResponseCookie.from("refresh-token", "")
                .httpOnly(true)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    @Override
    @Transactional
    public void deleteAllRefreshTokens(UUID userId, HttpServletResponse response) {
        refreshTokenRepository.deleteAllByUserId(userId);
        ResponseCookie cookie = ResponseCookie
                .from("refresh-token", "")
                .httpOnly(true)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();

        response.addHeader(
                HttpHeaders.SET_COOKIE,
                cookie.toString()
        );
    }

    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void cleanExpiredTokens() {
        refreshTokenRepository.deleteAllExpired(Instant.now());
    }
}
