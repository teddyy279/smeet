package com.karina.smeet.modules.notification.dto;

import com.karina.smeet.entity.postgre.Notification;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class NotificationResponse {
    private UUID id;
    private Notification.Type type;
    private UUID referenceId;
    private Notification.ReferenceType referenceType;
    private String title;
    private String body;
    private boolean isRead;
    private Instant createdAt;
}