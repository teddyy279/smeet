package com.karina.smeet.modules.room.repository;

import com.karina.smeet.entity.postgre.Roommember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RoomMemberRepository extends JpaRepository<Roommember, UUID> {
}
