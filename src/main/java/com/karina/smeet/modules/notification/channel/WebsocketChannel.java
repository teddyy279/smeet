package com.karina.smeet.modules.notification.channel;

import com.karina.smeet.modules.notification.dto.NotificationMessage;
import com.karina.smeet.modules.user.service.OnlineStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor

public class WebsocketChannel implements NotificationChannel {
    private final SimpMessagingTemplate messagingTemplate;
    private final OnlineStatusService onlineStatusService;

    @Override
    public boolean supports(NotificationMessage message) {
        return onlineStatusService.isOnline(message.getToUserId());
    }

    @Override
    public void send(NotificationMessage message) {
        messagingTemplate.convertAndSendToUser(
                message.getToUserId().toString(),
                "/queue/notification",
                message
        );
    }
}
