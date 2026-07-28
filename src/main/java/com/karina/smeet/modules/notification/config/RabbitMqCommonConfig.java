package com.karina.smeet.modules.notification.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.aopalliance.intercept.MethodInterceptor;

@Configuration
public class RabbitMQCommonConfig {

    public static final String COMMON_DLX = "common.dlx";
    public static final String COMMON_DLQ = "common.dlq";
    public static final String COMMON_DLQ_ROUTING_KEY = "dead-letter";

    private static final int MAX_RETRY_ATTEMPTS  = 3;
    private static final long INITIAL_BACKOFF_MS = 5_000;
    private static final double BACKOFF_MULT     = 2.0;
    private static final long MAX_BACKOFF_MS     = 30_000;

    public static Queue buildStandardQueue(String name, int ttlMs) {
        return QueueBuilder.durable(name)
                .withArgument("x-dead-letter-exchange", COMMON_DLX)
                .withArgument("x-dead-letter-routing-key", COMMON_DLQ_ROUTING_KEY)
                .withArgument("x-message-ttl", ttlMs)
                .build();
    }

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

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        template.setMandatory(true);

        template.setConfirmCallback((correlationData, ack, cause) -> {
            if (!ack) {
                System.err.println("[RabbitMQ] Message không đến exchange: " + cause);
            }
        });

        template.setReturnsCallback(returned ->
                System.err.println("[RabbitMQ] Message không route được: "
                        + returned.getRoutingKey())
        );

        return template;
    }

    // BẮT BUỘC — nếu thiếu bean này, @RabbitListener sẽ dùng
    // factory mặc định của Spring Boot, KHÔNG có retry theo code
    @Bean
    public MethodInterceptor retryInterceptor() {
        return RetryInterceptorBuilder.stateless()
                .maxRetries(MAX_RETRY_ATTEMPTS)
                .backOffOptions(INITIAL_BACKOFF_MS, BACKOFF_MULT, MAX_BACKOFF_MS)
                .recoverer(new RejectAndDontRequeueRecoverer())
                .build();
    }

    // TÊN BEAN PHẢI CHÍNH XÁC "rabbitListenerContainerFactory"
    // để @RabbitListener tự nhận, không cần chỉ định containerFactory
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory =
                new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter());
        factory.setAdviceChain(retryInterceptor());
        factory.setDefaultRequeueRejected(false);
        return factory;
    }
}