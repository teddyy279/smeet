package com.karina.smeet.modules.notification.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration

public class RabbitMqCommonConfig {
    public static final String COMMON_DLX = "common.dlx";
    public static final String COMMON_DLQ = "common.dlq";
    public static final String COMMON_DLQ_ROUTING_KEY = "dead-letter";

        private static final int MAX_RETRY_ATTEMPS = 3;
    private static final long INTITIAL_BACKOFF_MS = 5_000;
    private static final double BACKOFF_MULT = 2.0;
    private static long MAX_BACKOFF_MS = 30_000;

    public static Queue buildStandardQueue(String name, int ttlMs) {
        return QueueBuilder.durable(name)
                .withArgument("x-dead-letter-exchange", COMMON_DLX)
                .withArgument("x-dead-letter-routing-key", COMMON_DLQ_ROUTING_KEY)
                .withArgument("x-message-ttl", ttlMs)
                .build();
    }

    //Queue doesnt have TTL(message stays on util consumed)
    public static Queue buildPersistentQueue(String name) {
        return QueueBuilder.durable(name)
                .withArgument("x-dead-letter-exchange", COMMON_DLX)
                .withArgument("x-dead-letter-routing-key", COMMON_DLQ_ROUTING_KEY)
                .build();
    }

    @Bean
    public DirectExchange commonDlx() {
        return new DirectExchange(COMMON_DLX, true, false);
    }

    @Bean
    public Queue commonDlq() {
        return QueueBuilder.durable(COMMON_DLQ).build();
    }

    @Bean
    public Binding commonDlqBinding() {
        return BindingBuilder.bind(commonDlq())
                .to(commonDlx())
                .with(COMMON_DLQ_ROUTING_KEY);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new JacksonJsonMessageConverter();
    }
}
