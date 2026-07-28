package com.karina.smeet.modules.notification.service;

import com.karina.smeet.entity.postgre.Notification;
import com.karina.smeet.modules.notification.dto.NotificationMessage;
import com.karina.smeet.modules.notification.dto.NotificationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface NotificationService {
    Notification save(NotificationMessage message);

    Page<NotificationResponse> getMyNotifications(UUID userId, Pageable pageable);

    long countUnread(UUID userId);

    void markAsRead(UUID notificationId, UUID userId);

    void markAllAsRead(UUID userId);
}
