package com.karina.smeet.modules.room.repository;

import com.karina.smeet.entity.postgre.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
}
