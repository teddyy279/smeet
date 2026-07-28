package com.karina.smeet.modules.notification.dispatcher;

import com.karina.smeet.modules.notification.channel.NotificationChannel;
import com.karina.smeet.modules.notification.dto.NotificationMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j

public class NotificationDispatcher {
    private final List<NotificationChannel> channels;

    public void dispatch(NotificationMessage message) {
        channels.stream()
                .filter(channel -> channel.supports(message))
                .forEach(channel -> {
                    try {
                        channel.send(message);
                    } catch (Exception exception) {
                        log.error("Channel {} failed: {}",
                                channel.getClass().getSimpleName(), exception.getMessage());
                    }
                });
    }
}
