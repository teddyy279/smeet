package com.karina.smeet.modules.room.repository;

import com.karina.smeet.entity.postgre.RoomPinnedMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RoomPinnedMessageRepository extends JpaRepository<RoomPinnedMessage, UUID> {
    List<RoomPinnedMessage> findByRoom_IdOrderByPinnedAtDesc(UUID roomId);

    Optional<RoomPinnedMessage> findByRoom_IdAndMessageId(UUID roomId, String messageId);

    boolean existsByRoom_IdAndMessageId(UUID roomId, String messageId);

    long countByRoom_Id(UUID roomId);
}
