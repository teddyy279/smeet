package com.karina.smeet.modules.notification.producer;


import com.karina.smeet.modules.notification.config.NotificationRabbitMqConfig;
import com.karina.smeet.modules.notification.dto.NotificationMessage;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j

public class NotificationProducer {
    private final RabbitTemplate rabbitTemplate;

    public void send(NotificationMessage message) {
        rabbitTemplate.convertAndSend(
                NotificationRabbitMqConfig.NOTIFICATION_EXCHANGE,
                NotificationRabbitMqConfig.NOTIFICATION_ROUTING_KEY,
                message
        );
        log.info("Notification pushed to queue: type {}, toUser",
                message.getType(), message.getToUserId());
    }
}
