package com.karina.smeet.modules.notification.dto;

public record OtpEmailMessage(
   String toEmail,
   String otp
) {}
