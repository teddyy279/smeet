package com.karina.smeet.entity.postgre;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "notifications")
public class Notification {

    public enum Type {
        FRIEND_REQUEST,
        FRIEND_ACCEPTED,
        ROOM_INVITE,
        MISSED_CALL,
        MISSED_MESSAGE
    }

    public enum ReferenceType { ROOM, FRIEND, CALL }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    Type type;

    @Column(name = "reference_id")
    UUID referenceId;

    @Enumerated(EnumType.STRING)
    @Column(name = "reference_type", length = 10)
    ReferenceType referenceType;

    @Column(name = "title")
    String title;

    @Column(name = "body")
    String body;

    @Column(name = "is_read", nullable = false)
    @Builder.Default
    boolean isRead = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    Instant createdAt;
}