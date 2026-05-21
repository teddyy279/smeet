package com.karina.smeet.entity.postgre;


import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import com.karina.smeet.enums.Provider;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)

@Table(name = "user_auth_provider")
public class UserAuthProvider {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    Provider provider;

    @Column(name = "provider_id")
    String providerId;

    @Column(name = "password_hash")
    String passwordHash;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
}
