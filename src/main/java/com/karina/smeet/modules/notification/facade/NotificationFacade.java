package com.karina.smeet.modules.notification.facade;

import com.karina.smeet.entity.postgre.User;
import com.karina.smeet.modules.notification.dto.OtpEmailMessage;

import java.util.UUID;

public interface NotificationFacade {
    void sendOtpEmail(String email, String otp);
    void friendRequestSent(UUID toUserId, User from);
    void friendRequestAccepted(UUID toUserId, User from);
    void roomInvited(UUID toUserId, UUID roomId, String roomName);
    void missedCall(UUID toUserId, UUID roomId, String callerName);
    void missedMessage(UUID toUserId, String roomId, String senderName, String content);
}
