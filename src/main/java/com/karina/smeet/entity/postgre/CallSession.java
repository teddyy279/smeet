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
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder

@Table(name = "call_sessions")
public class Callsession {
    public enum Status { ONGOING, ENDED, MISSED }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    Room room;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "started_by")
    User startedBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    Status status;

    @CreationTimestamp
    @Column(name = "started_at", updatable = false)
    private Instant startedAt;

    @Column(name = "ended_at")
    private Instant endedAt;
}
