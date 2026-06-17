package com.karina.smeet.modules.auth.controller;

import com.karina.smeet.common.response.ApiResponse;
import com.karina.smeet.modules.auth.dto.request.LoginRequest;
import com.karina.smeet.modules.auth.dto.request.OtpRequest;
import com.karina.smeet.modules.auth.dto.request.RegisterRequest;
import com.karina.smeet.modules.auth.dto.request.ResetPasswordRequest;
import com.karina.smeet.modules.auth.dto.response.AuthResponse;
import com.karina.smeet.modules.auth.dto.response.RefreshTokenResponse;
import com.karina.smeet.modules.auth.service.AuthService;
import com.karina.smeet.modules.auth.service.OtpService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)

@RequestMapping("/auth")
public class AuthController {
    AuthService authService;
    OtpService otpService;

    @PostMapping("/register")
    public ApiResponse<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request, HttpServletResponse response) {
        return ApiResponse.<AuthResponse>builder()
                .result(authService.register(request, response))
                .build();
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(
            @Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        return ApiResponse.<AuthResponse>builder()
                .result(authService.login(request, response))
                .build();
    }

    @PostMapping("/refresh")
    public ApiResponse<RefreshTokenResponse> refresh(HttpServletRequest request) {
        return ApiResponse.<RefreshTokenResponse>builder()
                .result(authService.refreshToken(request))
                .build();
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        authService.logout(request, response);
        return ApiResponse.<Void>builder()
                .message("logout successfully!")
                .build();
    }

    @PostMapping("/logout-all")
    public ApiResponse<Void> logoutAll(@AuthenticationPrincipal UUID userId, HttpServletResponse response) {
        authService.logoutAll(userId, response);
        return ApiResponse.<Void>builder()
                .message("Logged out from all devices successfully")
                .build();
    }

    @PostMapping("/send-otp")
    public ApiResponse<Void> sendOtp(@Valid @RequestBody OtpRequest request) {
        otpService.sendOtp(request);
        return ApiResponse.<Void>builder()
                .message("The OTP code has been sent, check your email now!")
                .build();
    }

    @PostMapping("/reset-password")
    public ApiResponse<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        otpService.resetPassword(request);
        return ApiResponse.<Void>builder()
                .message("Your password has been reset, please log in again!")
                .build();
    }

    @PostMapping("/outbound/authentication")
    public ApiResponse<AuthResponse> outboundAuthenticate(
            @RequestParam("code") String code, HttpServletResponse response) {
        return ApiResponse.<AuthResponse>builder()
                .result(authService.outboundAuthenticate(code, response))
                .build();
    }
}
