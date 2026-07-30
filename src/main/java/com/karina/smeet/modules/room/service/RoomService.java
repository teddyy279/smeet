package com.karina.smeet.modules.room.service;

import com.karina.smeet.entity.postgre.Roommember;
import com.karina.smeet.modules.room.dto.request.CreateRoomRequest;
import com.karina.smeet.modules.room.dto.request.UpdateRoomRequest;
import com.karina.smeet.modules.room.dto.response.RoomDetailResponse;
import com.karina.smeet.modules.room.dto.response.RoomResponse;

import java.util.List;
import java.util.UUID;

public interface RoomService {
    RoomResponse createRoom(UUID currentUserId, CreateRoomRequest request);

    // filter: "all" (DIRECT+GROUP, sorted by last message), "group" (GROUP only), "unread"
    List<RoomResponse> getMyRooms(UUID currentUserId, String filter);

    RoomDetailResponse getRoomDetail(UUID roomId, UUID currentUserId);


    //just OWNER OR ADMIN have permission
    RoomResponse updateRoom(UUID roomId, UUID currentUserId, UpdateRoomRequest request);

    void addMember(UUID roomId, UUID currentUserId, UUID targetUserId);

    void kickMember(UUID roomId, UUID currentUserId, UUID targetUserId);

    void leaveRoom(UUID roomId, UUID currentUserId);

    void updateMemberRole(UUID roomId, UUID currentUserId, UUID targetUserId, Roommember.Role newRole);

    void deleteRoom(UUID roomId, UUID currentUserId);
}
