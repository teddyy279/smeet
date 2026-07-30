package com.karina.smeet.modules.room.repository;

import com.karina.smeet.entity.postgre.Roommember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RoomMemberRepository extends JpaRepository<Roommember, UUID> {
    Optional<Roommember> findByRoom_IdAndUser_Id(UUID roomId, UUID userId);

    List<Roommember> findByRoom_Id(UUID roomId);

    boolean existsByRoom_IdAndUser_Id(UUID roomId, UUID userId);

    long countByRoom_Id(UUID roomId);

    // Used to resolve "my role" for a whole page of rooms in a single query.
    List<Roommember> findByRoom_IdInAndUser_Id(List<UUID> roomIds, UUID userId);

    List<Roommember> findByUser_Id(UUID userId);

    // Used to resolve the "other member" of every DIRECT room in a page in one query,
    // instead of one findByRoom_Id call per DIRECT room.
    List<Roommember> findByRoom_IdIn(List<UUID> roomIds);

    // Used to resolve member counts for a whole page of rooms in a single query;
    // row[0] is the room id (UUID), row[1] is the count (Long).
    @Query("""
           SELECT m.room.id, COUNT(m)
           FROM Roommember m
           WHERE m.room.id IN :roomIds
           GROUP BY m.room.id
           """)
    List<Object[]> countByRoomIds(@Param("roomIds") List<UUID> roomIds);
}
