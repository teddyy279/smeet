package com.karina.smeet.modules.notification.facade;

import com.karina.smeet.modules.notification.dto.OtpEmailMessage;
import com.karina.smeet.modules.notification.producer.EmailProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationFacadeImpl implements NotificationFacade{
    private final EmailProducer emailProducer;

    @Override
    public void sendOtpEmail(String email, String otp) {
        OtpEmailMessage message = new OtpEmailMessage(email, otp);
        emailProducer.sendOtpEmail(message);
    }
}
