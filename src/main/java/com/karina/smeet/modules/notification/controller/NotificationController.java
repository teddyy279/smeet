package com.karina.smeet.modules.notification.controller;

import com.karina.smeet.common.response.ApiResponse;
import com.karina.smeet.modules.notification.dto.NotificationResponse;
import com.karina.smeet.modules.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor


public class NotificationController {
    private final NotificationService notificationService;

    @GetMapping
    public ApiResponse<Page<NotificationResponse>> getMyNotification(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UUID userId) {

        Pageable pageable = PageRequest.of(page, size);
        return ApiResponse.<Page<NotificationResponse>>builder()
                .result(notificationService.getMyNotifications(userId, pageable))
                .build();
    }

    @GetMapping("/unread-count")
    public ApiResponse<Long> countUnread(@AuthenticationPrincipal UUID userId) {
        return ApiResponse.<Long>builder()
                .result(notificationService.countUnread(userId))
                .build();
    }

    @PatchMapping("/{notificationId}/read")
    public ApiResponse<Void> markAsRead(
            @PathVariable UUID notificationId,
            @AuthenticationPrincipal UUID userId) {

        notificationService.markAsRead(notificationId, userId);
        return ApiResponse.<Void>builder()
                .message("Marked as done")
                .build();
    }

    @PatchMapping("/read-all")
    public ApiResponse<Void> markAllAsRead(@AuthenticationPrincipal UUID userId) {
        notificationService.markAllAsRead(userId);
        return ApiResponse.<Void>builder()
                .message("Marked all as read")
                .build();
    }

}
