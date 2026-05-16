package com.karina.smeet.entity.mongo;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "messages")
@CompoundIndexes({
        @CompoundIndex(name = "idx_room_time",   def = "{'roomId': 1, 'createdAt': -1}"),
        @CompoundIndex(name = "idx_room_active", def = "{'roomId': 1, 'deletedAt': 1, 'createdAt': -1}")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)


public class Message {

    public enum Type { TEXT, IMAGE, FILE, CALL }

    @Id
    String id;

    String roomId;
    String senderId;

    String senderName;
    String senderAvatar;

    Type type;

    @TextIndexed
    String content;

    @Builder.Default
    List<String> mediaUrls = new ArrayList<>();

    String replyTo;

    @Builder.Default
    List<Reaction> reactions = new ArrayList<>();

    @Builder.Default
    List<SeenBy> seenBy = new ArrayList<>();


    @CreatedDate
    Instant createdAt;

    Instant deletedAt;


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Reaction {
        String userId;
        String emoji;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SeenBy {
        String userId;
        Instant seenAt;
    }
}