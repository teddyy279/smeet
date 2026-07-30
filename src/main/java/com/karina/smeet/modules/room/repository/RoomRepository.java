package com.karina.smeet.modules.room.repository;

import com.karina.smeet.entity.postgre.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RoomRepository extends JpaRepository<Room, UUID> {
    @Query("""
          SELECT r FROM Room r
          JOIN r.members m1
          JOIN r.members m2
          WHERE r.type = 'DIRECT'
          AND m1.user.id = :userA
          AND m2.user.id = :userB 
          """)
    Optional<Room> findDirectRoom(
            @Param("userA") UUID userA,
            @Param("userB") UUID userB
    );

    @Query("""
           SELECT r FROM Room r
           JOIN r.members m
           WHERE m.user.id = :userId
           AND r.type = 'GROUP'
           AND immutable_unaccent(LOWER(r.name)) LIKE immutable_unaccent(LOWER(CONCAT('%', :query, '%')))
           """)
    List<Room> searchGroupRooms(
            @Param("query") String query,
            @Param("userId") UUID userId
    );

    // Returns every room (DIRECT + GROUP) the user belongs to. Sorting by last-message time
    // happens in RoomServiceImpl afterwards since that data lives in MongoDB, not Postgres.
    @Query("""
            SELECT r FROM Room r
            JOIN r.members m
            WHERE m.user.id = :userId
            """)
    List<Room> findAllRoomsByUserId(@Param("userId") UUID userId);
}
