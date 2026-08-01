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

@Table(name = "room_pinned_messages",
        uniqueConstraints = @UniqueConstraint(columnNames = {"room_id", "message_id"}))

public class RoomPinnedMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    Room room;

    @Column(name = "message_id", nullable = false)
    String messageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pinned_by", nullable = false)
    User pinnedBy;

    @CreationTimestamp
    @Column(name = "pinned_at", updatable = false)
    Instant pinnedAt;
}
