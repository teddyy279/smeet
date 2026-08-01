package com.karina.smeet.modules.chat.service;

import com.karina.smeet.modules.chat.dto.request.SendMessageRequest;
import com.karina.smeet.modules.chat.dto.response.ChatMessageResponse;
import org.springframework.data.domain.Page;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface ChatService {
    ChatMessageResponse sendMessage(String roomId, UUID senderId, SendMessageRequest request);

    void markAsRead(String roomId, UUID userId);

    ChatMessageResponse addReaction(String messageId, UUID userId, String emoji);

    void deleteMessage(String messageId, UUID userId);

    void hideMessageForMe(String messageId, UUID userId);

    List<ChatMessageResponse> forwardMessage(String messageId, UUID senderId, List<String> targetRoomIds);

    void pinMessage(String roomId, String messageId, UUID userId);
    void unpinMessage(String roomId, String messageId, UUID userId);
    List<ChatMessageResponse> getPinnedMessages(String roomId, UUID userId);

    List<ChatMessageResponse> getHistory(
            String roomId, UUID userId, Instant before, String beforeId, int size);

    Map<UUID, Long> getUnreadCountByRoom(UUID userId);
}
