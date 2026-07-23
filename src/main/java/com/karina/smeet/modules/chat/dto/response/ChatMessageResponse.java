package com.karina.smeet.modules.chat.dto.response;

import com.karina.smeet.entity.mongo.Message;
import lombok.Builder;

import java.time.Instant;
import java.util.List;

@Builder
public record ChatMessageResponse(
        String id,
        String roomId,

        String senderId,
        String senderName,
        String senderAvatar,

        Message.Type type,
        String content,
        List<String> mediaUrls,
        Integer durationSeconds,
        String replyTo,

        List<ReactionDto> reactions,
        List<SeenByDto> seenBy,

        Instant createdAt
) {

    @Builder
    public record ReactionDto(String userId, String emoji) {
    }

    @Builder
    public record SeenByDto(String userId, Instant seenAt) {
    }
}
