package com.karina.smeet.modules.user.controller;

import com.karina.smeet.common.response.ApiResponse;
import com.karina.smeet.modules.user.dto.request.ChangePasswordRequest;
import com.karina.smeet.modules.user.dto.request.UpdateProfileRequest;
import com.karina.smeet.modules.user.dto.response.AvatarUploadResponse;
import com.karina.smeet.modules.user.dto.response.MyProfileResponse;
import com.karina.smeet.modules.user.dto.response.UserProfileResponse;
import com.karina.smeet.modules.user.service.AvatarService;
import com.karina.smeet.modules.user.service.UserService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/users")
public class UserController {
    UserService userService;
    AvatarService avatarService;

    @GetMapping("/me")
    public ApiResponse<MyProfileResponse> getMyProfile(
            @AuthenticationPrincipal UUID userId) {
        return ApiResponse.<MyProfileResponse>builder()
                .result(userService.getMyProfile(userId))
                .build();
    }

    @PatchMapping("/me")
    public ApiResponse<MyProfileResponse> updateProfile(
            @AuthenticationPrincipal UUID userId,
            @Valid @RequestBody UpdateProfileRequest request) {
        return ApiResponse.<MyProfileResponse>builder()
                .result(userService.updateProfile(userId, request))
                .build();
    }

    @PatchMapping("/me/password")
    public ApiResponse<Void> changePassword(
            @AuthenticationPrincipal UUID userId,
            @Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(userId, request);
        return ApiResponse.<Void>builder()
                .message("Password successfully changed!")
                .build();
    }

    @PostMapping(value = "/me/avatar", consumes = "multipart/form-data")
    public ApiResponse<String> uploadAvatar(
            @AuthenticationPrincipal UUID userId,
            @RequestParam("file") MultipartFile file) {

        String avatarUrl = avatarService.uploadAvatar(userId, file);
        userService.updateAvatarUrl(userId, avatarUrl);

        return ApiResponse.<String>builder()
                .result(userService.updateAvatarUrl(userId, avatarUrl))
                .build();
    }

    @GetMapping("/find")
    public ApiResponse<UserProfileResponse> findByUsername(
            @RequestParam String username,
            @AuthenticationPrincipal UUID currentUserId) {
        return ApiResponse.<UserProfileResponse>builder()
                .result(userService.findByUsername(username, currentUserId))
                .build();
    }


    @GetMapping("/{userId}")
    public ApiResponse<UserProfileResponse> getUserProfile(
            @PathVariable UUID userId,
            @AuthenticationPrincipal UUID currentUserId) {
        return ApiResponse.<UserProfileResponse>builder()
                .result(userService.getUserProfile(userId, currentUserId))
                .build();
    }

    @PostMapping("/me/heartbeat")
    public ApiResponse<Void> heartbeat(@AuthenticationPrincipal UUID userId) {
        userService.heartbeat(userId);
        return ApiResponse.<Void>builder().build();
    }
}