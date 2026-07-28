package com.karina.smeet.modules.notification.dto;

import com.karina.smeet.entity.postgre.Notification;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationMessage implements Serializable {

    private UUID toUserId;
    private Notification.Type type;
    private UUID referenceId;
    private Notification.ReferenceType referenceType;
    private String title;
    private String body;
}