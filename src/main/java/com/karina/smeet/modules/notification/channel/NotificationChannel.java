package com.karina.smeet.modules.notification.channel;

import com.karina.smeet.modules.notification.dto.NotificationMessage;

public interface NotificationChannel {
    boolean supports(NotificationMessage message);
    void send(NotificationMessage message);
}
