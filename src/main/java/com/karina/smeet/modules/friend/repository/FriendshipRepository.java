package com.karina.smeet.modules.friend.repository;

import com.karina.smeet.entity.postgre.Friendship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface FriendshipRepository extends JpaRepository<Friendship, UUID> {
    @Query()
    Optional<Friendship> findBetween(
            @Param("userA") UUID userA,
            @Param("userB") UUID userB
    );
}
