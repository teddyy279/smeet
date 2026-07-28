package com.karina.smeet.modules.notification.consumer;

import com.karina.smeet.modules.notification.config.RabbitMQCommonConfig;
import com.karina.smeet.modules.notification.dto.NotificationMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.amqp.core.Message;

@Component
@RequiredArgsConstructor
@Slf4j

public class DeadLetterConsumer {
    @RabbitListener(queues = RabbitMQCommonConfig.COMMON_DLQ)
    public void handleFailedNotification(NotificationMessage message, Message rawMessage) {
        String reason = getDeathReason(rawMessage);
        int deathCount = getDeathCount(rawMessage);

        log.error(
                "Notification",
                deathCount,
                message.getType(),
                message.getToUserId(),
                reason
        );
    }




    private String getDeathReason(Message message) {
        try {
            var deaths = message.getMessageProperties()
                    .getXDeathHeader();

            if (deaths != null && !deaths.isEmpty()) {
                return String.valueOf(deaths.get(0).get("reson"));
            }
        } catch (Exception ignore) {
        //    return "unknown";
        }
        return "unknown";
    }

    private int getDeathCount(Message message) {
        try {
            var deaths = message.getMessageProperties().getXDeathHeader();
            if (deaths != null && !deaths.isEmpty()) {
                return ((Long) deaths.get(0).get("count")).intValue();
            }
        } catch (Exception ignored) {}
        return 0;
    }
}
