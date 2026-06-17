package com.karina.smeet.modules.notification.producer;


import com.karina.smeet.modules.notification.config.EmailRabbitMQConfig;
import com.karina.smeet.modules.notification.dto.OtpEmailMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j

public class EmailProducer {
    private final RabbitTemplate rabbitTemplate;

    public void sendOtpEmail(OtpEmailMessage request) {
        rabbitTemplate.convertAndSend(
                EmailRabbitMQConfig.EMAIL_EXCHANGE,
                EmailRabbitMQConfig.EMAIL_OTP_ROUTING_KEY,
                request
        );

        log.info("OTP email pushed to queue: {}", request.toEmail());
    }
}
