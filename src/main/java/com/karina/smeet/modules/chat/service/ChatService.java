package com.karina.smeet.modules.chat.service;

import com.karina.smeet.modules.chat.dto.request.SendMessageRequest;
import com.karina.smeet.modules.chat.dto.response.ChatMessageResponse;
import org.springframework.data.domain.Page;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface ChatService {
    ChatMessageResponse sendMessage(String roomId, UUID senderId, SendMessageRequest request);

    // đánh dấu đã đọc — update last_read_at + seenBy
    void markAsRead(String roomId, UUID userId);

    // thả/đổi reaction cho 1 message
    ChatMessageResponse addReaction(String messageId, UUID userId, String emoji);

    // thu hồi tin nhắn — chỉ sender được xóa, soft delete
    void deleteMessage(String messageId, UUID userId);

    // load lịch sử chat — phân trang
    List<ChatMessageResponse> getHistory(
            String roomId, UUID userId, Instant before, String beforeId, int size);
}
