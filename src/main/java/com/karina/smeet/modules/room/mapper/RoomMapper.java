package com.karina.smeet.modules.room.mapper;

import com.karina.smeet.entity.mongo.Message;
import com.karina.smeet.entity.postgre.Room;
import com.karina.smeet.entity.postgre.Roommember;
import com.karina.smeet.entity.postgre.User;
import com.karina.smeet.modules.chat.repository.MessageRepository;
import com.karina.smeet.modules.chat.repository.MessageRepository.RoomLastMessage;
import com.karina.smeet.modules.room.dto.response.RoomDetailResponse;
import com.karina.smeet.modules.room.dto.response.RoomDetailResponse.RoomMemberResponse;
import com.karina.smeet.modules.room.dto.response.RoomResponse;
import com.karina.smeet.modules.room.repository.RoomMemberRepository;
import com.karina.smeet.modules.user.service.OnlineStatusService;
import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.query.Criteria;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public abstract class RoomMapper {

    @Autowired
    protected RoomMemberRepository roomMemberRepository;

    @Autowired
    protected OnlineStatusService onlineStatusService;

    @Autowired
    protected MessageRepository messageRepository;

    @Autowired
    protected MongoTemplate mongoTemplate;

    public RoomResponse toRoomResponse(Room room, UUID currentUserId) {
        Roommember me = roomMemberRepository
                .findByRoom_IdAndUser_Id(room.getId(), currentUserId)
                .orElse(null);

        Message lastMessage = messageRepository
                .findFirstByRoomIdAndDeletedAtNullOrderByCreatedAtDesc(room.getId().toString())
                .orElse(null);

        long unread = 0;

        if (me != null) {
            Instant lastRead = me.getLastReadAt() != null ? me.getLastReadAt() : Instant.EPOCH;
            unread = messageRepository.countByRoomIdAndCreatedAtAfterAndDeletedAtIsNull(room.getId().toString(), lastRead);
        }

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
                .lastMessagePreview(lastMessage != null ? buildPreview(lastMessage) : null)
                .lastMessageAt(lastMessage != null ? lastMessage.getCreatedAt() : null)
                .unreadCount(unread)
                .build();
    }

    // Batched version of toRoomResponse for listing endpoints: resolves "my role", member counts,
    // last message and unread count for every room in a constant number of queries instead of
    // ~4 queries per room.
    public List<RoomResponse> toRoomResponses(List<Room> rooms, UUID currentUserId) {
        if (rooms.isEmpty()) return List.of();

        List<UUID> roomIds = rooms.stream().map(Room::getId).toList();

        List<Roommember> myMemberships = roomMemberRepository.findByRoom_IdInAndUser_Id(roomIds, currentUserId);
        Map<UUID, Roommember.Role> myRoles = new HashMap<>();
        for (Roommember m : myMemberships) {
            myRoles.put(m.getRoom().getId(), m.getRole());
        }

        Map<UUID, Long> memberCounts = new HashMap<>();
        for (Object[] row : roomMemberRepository.countByRoomIds(roomIds)) {
            memberCounts.put((UUID) row[0], (Long) row[1]);
        }

        List<String> roomIdStrings = roomIds.stream().map(UUID::toString).toList();

        Map<String, Message> lastMessages = messageRepository.findLastMessagesByRoomIds(roomIdStrings).stream()
                .collect(Collectors.toMap(RoomLastMessage::roomId, RoomLastMessage::doc));

        Map<String, Long> unreadCounts = countUnreadByRoom(myMemberships);

        // "Other member" of every DIRECT room, resolved in one query instead of one per room.
        List<UUID> directRoomIds = rooms.stream()
                .filter(r -> r.getType() == Room.Type.DIRECT)
                .map(Room::getId)
                .toList();
        Map<UUID, User> otherByRoom = new HashMap<>();
        if (!directRoomIds.isEmpty()) {
            for (Roommember m : roomMemberRepository.findByRoom_IdIn(directRoomIds)) {
                if (!m.getUser().getId().equals(currentUserId)) {
                    otherByRoom.put(m.getRoom().getId(), m.getUser());
                }
            }
        }

        return rooms.stream()
                .map(room -> {
                    User other = otherByRoom.get(room.getId());
                    Message lastMessage = lastMessages.get(room.getId().toString());

                    return RoomResponse.builder()
                            .id(room.getId())
                            .type(room.getType())
                            .name(resolveRoomName(room, other))
                            .avatarUrl(resolveRoomAvatar(room, other))
                            .memberCount(memberCounts.getOrDefault(room.getId(), 0L).intValue())
                            .createdAt(room.getCreatedAt())
                            .myRole(myRoles.get(room.getId()))
                            .lastMessagePreview(lastMessage != null ? buildPreview(lastMessage) : null)
                            .lastMessageAt(lastMessage != null ? lastMessage.getCreatedAt() : null)
                            .unreadCount(unreadCounts.getOrDefault(room.getId().toString(), 0L))
                            .build();
                })
                .toList();
    }

    // Computes unread count per room in a single aggregation even though each room has a
    // different "unread since" threshold (member's lastReadAt): one $or clause per room.
    private Map<String, Long> countUnreadByRoom(List<Roommember> memberships) {
        if (memberships.isEmpty()) return Map.of();

        List<Criteria> perRoomThreshold = memberships.stream()
                .map(m -> Criteria.where("roomId").is(m.getRoom().getId().toString())
                        .and("createdAt").gt(m.getLastReadAt() != null ? m.getLastReadAt() : Instant.EPOCH))
                .toList();

        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(new Criteria().andOperator(
                        Criteria.where("deletedAt").is(null),
                        new Criteria().orOperator(perRoomThreshold.toArray(new Criteria[0]))
                )),
                Aggregation.group("roomId").count().as("unread")
        );

        /*WHERE deletedAt IS NULL
        AND (
                (roomId = 'room1' AND createdAt > t1)
        OR (roomId = 'room2' AND createdAt > t2)
        OR (roomId = 'room3' AND createdAt > t3))
        */

        AggregationResults<UnreadCount> results =
                mongoTemplate.aggregate(aggregation, "messages", UnreadCount.class);

        Map<String, Long> unreadByRoom = new HashMap<>();
        for (UnreadCount r : results.getMappedResults()) {
            unreadByRoom.put(r.roomId(), r.unread());
        }
        return unreadByRoom;
    }

    private record UnreadCount(@Field("_id") String roomId, long unread) {}

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

    private String buildPreview(Message message) {
        return switch (message.getType()) {
            case TEXT -> message.getContent();
            case IMAGE -> "[IMAGE]";
            case FILE -> "[attachment]";
            case AUDIO -> "[Voice message]";
            case CALL -> "Missed call";
        };
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
