package com.karina.smeet.modules.notification.config;


import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EmailRabbitMQConfig {
    public static final String EMAIL_EXCHANGE = "email.exchange";
    public static final String EMAIL_OTP_QUEUE = "email.otp.queue";
    public static final String EMAIL_OTP_ROUTING_KEY = "email.otp";
    public static final String EMAIL_WELCOME_QUEUE = "email.welcom.queue";
    public static final String EMAIL_WELCOME_ROUTING_KEY = "email.welcome";

    //private static final int OTP_TTL =
    private static final int WELCOME_TTL = 30 * 60 * 1000;

    @Bean
    public DirectExchange emailExchange() {
        return new DirectExchange(EMAIL_EXCHANGE, true, false);
    }

    @Bean
    public Queue emailOtpQueue() {
        return QueueBuilder.durable(EMAIL_OTP_QUEUE).build();
    }

    @Bean
    public Queue emailWelcomeQueue() {
        return RabbitMqCommonConfig.buildStandardQueue(EMAIL_WELCOME_QUEUE, WELCOME_TTL);
    }

    @Bean
    public Binding  emailOtpBinding() {
        return BindingBuilder.bind(emailOtpQueue())
                .to(emailExchange())
                .with(EMAIL_OTP_ROUTING_KEY);
    }

    @Bean
    public Binding emailWelcomeBinding() {
        return BindingBuilder.bind(emailWelcomeQueue())
                .to(emailExchange())
                .with(EMAIL_WELCOME_ROUTING_KEY);
    }
}
