package com.karina.smeet.modules.notification.facade;

import com.karina.smeet.modules.notification.dto.OtpEmailMessage;

public interface NotificationFacade {
    void sendOtpEmail(String email, String otp);
}
