package com.karina.smeet.modules.room.mapper;

import com.karina.smeet.entity.postgre.Room;
import com.karina.smeet.entity.postgre.Roommember;
import com.karina.smeet.entity.postgre.User;
import com.karina.smeet.modules.room.dto.response.RoomDetailResponse;
import com.karina.smeet.modules.room.dto.response.RoomDetailResponse.RoomMemberResponse;
import com.karina.smeet.modules.room.dto.response.RoomResponse;
import com.karina.smeet.modules.room.repository.RoomMemberRepository;
import com.karina.smeet.modules.user.service.OnlineStatusService;
import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Mapper(componentModel = "spring")
public abstract class RoomMapper {

    @Autowired
    protected RoomMemberRepository roomMemberRepository;

    @Autowired
    protected OnlineStatusService onlineStatusService;

    public RoomResponse toRoomResponse(Room room, UUID currentUserId) {
        Roommember me = roomMemberRepository
                .findByRoom_IdAndUser_Id(room.getId(), currentUserId)
                .orElse(null);

        User other = room.getType() == Room.Type.DIRECT
                ? findOtherMember(room, currentUserId).orElse(null)
                : null;

        return RoomResponse.builder()
                .id(room.getId())
                .type(room.getType())
                .name(resolveRoomName(room, other))
                .avatarUrl(resolveRoomAvatar(room, other))
                .memberCount((int) roomMemberRepository.countByRoom_Id(room.getId()))
                .createdAt(room.getCreatedAt())
                .myRole(me != null ? me.getRole() : null)
                .build();
    }

    // Batched version of toRoomResponse for listing endpoints: resolves "my role" and member
    // counts for every room in 2 queries total instead of 2 queries per room.
    public List<RoomResponse> toRoomResponses(List<Room> rooms, UUID currentUserId) {
        if (rooms.isEmpty()) return List.of();

        List<UUID> roomIds = rooms.stream().map(Room::getId).toList();

        Map<UUID, Roommember.Role> myRoles = new HashMap<>();
        for (Roommember m : roomMemberRepository.findByRoom_IdInAndUser_Id(roomIds, currentUserId)) {
            myRoles.put(m.getRoom().getId(), m.getRole());
        }

        Map<UUID, Long> memberCounts = new HashMap<>();
        for (Object[] row : roomMemberRepository.countByRoomIds(roomIds)) {
            memberCounts.put((UUID) row[0], (Long) row[1]);
        }

        return rooms.stream()
                .map(room -> {
                    // Callers of this batch path are expected to pass GROUP rooms only (e.g. group
                    // room listings) — DIRECT rooms would reintroduce a per-room lookup here.
                    User other = room.getType() == Room.Type.DIRECT
                            ? findOtherMember(room, currentUserId).orElse(null)
                            : null;

                    return RoomResponse.builder()
                            .id(room.getId())
                            .type(room.getType())
                            .name(resolveRoomName(room, other))
                            .avatarUrl(resolveRoomAvatar(room, other))
                            .memberCount(memberCounts.getOrDefault(room.getId(), 0L).intValue())
                            .createdAt(room.getCreatedAt())
                            .myRole(myRoles.get(room.getId()))
                            .build();
                })
                .toList();
    }

    public RoomDetailResponse toRoomDetailResponse(Room room, UUID currentUserId) {
        Roommember me = roomMemberRepository
                .findByRoom_IdAndUser_Id(room.getId(), currentUserId)
                .orElse(null);

        List<Roommember> members = roomMemberRepository.findByRoom_Id(room.getId());

        Map<UUID, Boolean> onlineStatuses = onlineStatusService.isOnline(
                members.stream().map(m -> m.getUser().getId()).toList());

        User other = room.getType() == Room.Type.DIRECT
                ? members.stream()
                        .map(Roommember::getUser)
                        .filter(u -> !u.getId().equals(currentUserId))
                        .findFirst()
                        .orElse(null)
                : null;

        List<RoomMemberResponse> memberResponses = members.stream()
                .map(m -> toRoomMemberResponse(m, onlineStatuses))
                .toList();

        return RoomDetailResponse.builder()
                .id(room.getId())
                .type(room.getType())
                .name(resolveRoomName(room, other))
                .avatarUrl(resolveRoomAvatar(room, other))
                .createdAt(room.getCreatedAt())
                .myRole(me != null ? me.getRole() : null)
                .members(memberResponses)
                .build();
    }

    protected RoomMemberResponse toRoomMemberResponse(Roommember m, Map<UUID, Boolean> onlineStatuses) {
        return RoomMemberResponse.builder()
                .userId(m.getUser().getId())
                .username(m.getUser().getUsername())
                .displayName(m.getUser().getDisplayName())
                .avatarUrl(m.getUser().getAvatarUrl())
                .isOnline(Boolean.TRUE.equals(onlineStatuses.get(m.getUser().getId())))
                .role(m.getRole())
                .joinedAt(m.getJoinedAt())
                .build();
    }

    private String resolveRoomName(Room room, User other) {
        if (room.getType() == Room.Type.GROUP) return room.getName();
        return other != null ? other.getDisplayName() : "Unknown";
    }

    private String resolveRoomAvatar(Room room, User other) {
        if (room.getType() == Room.Type.GROUP) return room.getAvatarUrl();
        return other != null ? other.getAvatarUrl() : null;
    }

    private Optional<User> findOtherMember(Room room, UUID currentUserId) {
        return roomMemberRepository.findByRoom_Id(room.getId()).stream()
                .map(Roommember::getUser)
                .filter(u -> !u.getId().equals(currentUserId))
                .findFirst();
    }
}
