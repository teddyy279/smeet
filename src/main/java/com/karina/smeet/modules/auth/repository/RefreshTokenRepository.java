package com.karina.smeet.modules.auth.repository;

import com.karina.smeet.entity.postgre.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
    Optional<RefreshToken> findByToken(String token);

    void deleteAllByUserId(UUID userId);

    void deleteByToken(String token);

    @Modifying
    @Query("""
        DELETE FROM RefreshToken r
        WHERE r.expiryTime < :now
    """)
    void deleteAllExpired(@Param("now") Instant now);
}
