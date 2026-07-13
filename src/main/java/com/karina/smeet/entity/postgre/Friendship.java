package com.karina.smeet.entity.postgre;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "friendships",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_friendship",
                        columnNames = {"requester_id", "addressee_id"}
                ),
                @UniqueConstraint(
                        name = "uq_friendship_pair",
                        columnNames = {"user_low_id", "user_high_id"}
                )
        },
        indexes = {
                @Index(name = "idx_friendship_requester", columnList = "requester_id"),
                @Index(name = "idx_friendship_addressee", columnList = "addressee_id")
        }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)

public class Friendship {
    public enum Status { PENDING, ACCEPTED, REJECTED }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", nullable = false)
    User requester;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "addressee_id", nullable = false)
    User addressee;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    Status status;

    @Column(name = "user_low_id", nullable = false, updatable = false)
    UUID userLowId;

    @Column(name = "user_high_id", nullable = false, updatable = false)
    UUID userHighId;

    @Version
    @Column(nullable = false)
    Long version;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    private void normalizePair() {
        UUID a = requester.getId();
        UUID b = addressee.getId();
        if (a.compareTo(b) <= 0) {
            userLowId = a;
            userHighId = b;
        } else {
            userLowId = b;
            userHighId = a;
        }
    }
}
