package com.karina.smeet.modules.notification.consumer;

import com.karina.smeet.entity.postgre.Notification;
import com.karina.smeet.modules.notification.config.NotificationRabbitMqConfig;
import com.karina.smeet.modules.notification.dispatcher.NotificationDispatcher;
import com.karina.smeet.modules.notification.dto.NotificationMessage;
import com.karina.smeet.modules.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j

public class NotificationConsumer {
    private final NotificationService notificationService;
    private final NotificationDispatcher dispatcher;

    @RabbitListener(queues = NotificationRabbitMqConfig.NOTIFICATION_QUEUE)
    public void consume(NotificationMessage message) {
        log.info("Consuming notification: type={}, toUser={}",
                message.getType(), message.getToUserId());

        if (message.getType() != Notification.Type.MISSED_MESSAGE) {
            notificationService.save(message);
        }
        dispatcher.dispatch(message);
    }
}
