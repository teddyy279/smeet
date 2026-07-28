package com.karina.smeet.modules.notification.service;

import com.karina.smeet.common.exception.AppException;
import com.karina.smeet.common.exception.ErrorCode;
import com.karina.smeet.entity.postgre.Notification;
import com.karina.smeet.entity.postgre.User;
import com.karina.smeet.modules.notification.dto.NotificationMessage;
import com.karina.smeet.modules.notification.dto.NotificationResponse;
import com.karina.smeet.modules.notification.repository.NotificationRepository;
import com.karina.smeet.modules.user.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)

public class NotificationServiceImpl implements NotificationService {
    NotificationRepository notificationRepository;
    UserRepository userRepository;

    @Override
    @Transactional
    public Notification save(NotificationMessage message) {
        User user = userRepository.findById(message.getToUserId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        Notification notification = Notification.builder()
                .user(user)
                .type(message.getType())
                .referenceId(message.getReferenceId())
                .referenceType(message.getReferenceType())
                .title(message.getTitle())
                .body(message.getBody())
                .isRead(false)
                .build();

        return notificationRepository.save(notification);
    }

    @Override
    public long countUnread(UUID userId) {
        return notificationRepository.countByUser_IdAndIsReadFalse(userId);
    }

    @Override
    public Page<NotificationResponse> getMyNotifications(UUID userId, Pageable pageable) {
        return notificationRepository
                .findByUser_IdOrderByCreatedAtDesc(userId, pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional
    public void markAsRead(UUID notificationId, UUID userId) {
        notificationRepository.markAsRead(notificationId, userId);
    }

    @Override
    @Transactional
    public void markAllAsRead(UUID userId) {
        notificationRepository.markAllAsRead(userId);
    }



    private NotificationResponse toResponse(Notification n) {
        return NotificationResponse.builder()
                .id(n.getId())
                .type(n.getType())
                .referenceId(n.getReferenceId())
                .referenceType(n.getReferenceType())
                .title(n.getTitle())
                .body(n.getBody())
                .isRead(n.isRead())
                .createdAt(n.getCreatedAt())
                .build();
    }
}


