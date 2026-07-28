package com.karina.smeet.modules.notification.config;


import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration

public class NotificationRabbitMqConfig {
    public static final String NOTIFICATION_EXCHANGE    = "notification.exchange";
    public static final String NOTIFICATION_QUEUE       = "notification.queue";
    public static final String NOTIFICATION_ROUTING_KEY = "notification";

    private static final int NOTIFICATION_TTL = 5 * 60 * 1000;

    @Bean
    public DirectExchange notificationExchange() {
        return new DirectExchange(NOTIFICATION_EXCHANGE, true, false);
    }

    @Bean
    public Queue notificationQueue() {
        return RabbitMQCommonConfig.buildStandardQueue(
                NOTIFICATION_QUEUE,
                NOTIFICATION_TTL
        );
    }

    @Bean
    public Binding notificationBinding() {
        return BindingBuilder.bind(notificationQueue())
                .to(notificationExchange())
                .with(NOTIFICATION_ROUTING_KEY);
    }
}
