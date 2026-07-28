package com.karina.smeet.modules.notification.facade;

import com.karina.smeet.entity.postgre.Notification;
import com.karina.smeet.entity.postgre.User;
import com.karina.smeet.modules.notification.dto.NotificationMessage;
import com.karina.smeet.modules.notification.dto.OtpEmailMessage;
import com.karina.smeet.modules.notification.producer.EmailProducer;
import com.karina.smeet.modules.notification.producer.NotificationProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class NotificationFacadeImpl implements NotificationFacade{
    private final EmailProducer emailProducer;
    private final NotificationProducer notificationProducer;

    @Override
    public void sendOtpEmail(String email, String otp) {
        OtpEmailMessage message = new OtpEmailMessage(email, otp);
        emailProducer.sendOtpEmail(message);
    }

    @Override
    public void friendRequestSent(UUID toUserId, User from) {
        notificationProducer.send(NotificationMessage.builder()
                        .toUserId(toUserId)
                        .type(Notification.Type.FRIEND_REQUEST)
                        .referenceId(from.getId())
                        .referenceType(Notification.ReferenceType.FRIEND)
                        .title("Friend request")
                        .body(from.getDisplayName() + " would like to be friends with you")
                        .build());
    }

    //public void

    @Override
    public void friendRequestAccepted(UUID toUserId, User from) {
        notificationProducer.send(NotificationMessage.builder()
                        .toUserId(toUserId)
                        .type(Notification.Type.FRIEND_ACCEPTED)
                        .referenceId(from.getId())
                        .referenceType(Notification.ReferenceType.FRIEND)
                        .title("The friend request has been accepted!")
                        .body(from.getDisplayName() + " accepted the friend request!")
                        .build());
    }

    public void roomInvited(UUID toUserId, UUID roomId, String roomName) {
        notificationProducer.send(NotificationMessage.builder()
                        .toUserId(toUserId)
                        .type(Notification.Type.ROOM_INVITE)
                        .referenceId(roomId)
                        .referenceType(Notification.ReferenceType.ROOM)
                        .title("Group invitation")
                        .body("You have been invited to the group " + roomName)
                        .build());
    }

    public void missedCall(UUID toUserId, UUID roomId, String callerName) {
        notificationProducer.send(NotificationMessage.builder()
                .toUserId(toUserId)
                .type(Notification.Type.MISSED_CALL)
                .referenceId(roomId)
                .referenceType(Notification.ReferenceType.CALL)
                .title("MISSED CALL")
                .body("You have a missed call from " + callerName)
                .build());
    }

    public void missedMessage(UUID toUserId, String roomId, String senderName, String content) {
        notificationProducer.send(NotificationMessage.builder()
                .toUserId(toUserId)
                .type(Notification.Type.MISSED_MESSAGE)
                .referenceId(UUID.fromString(roomId))
                .referenceType(Notification.ReferenceType.ROOM)
                .title(senderName)
                .body(content)
                .build());
    }

}
