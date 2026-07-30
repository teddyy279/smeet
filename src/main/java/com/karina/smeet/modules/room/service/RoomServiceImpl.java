package com.karina.smeet.modules.room.service;

import com.karina.smeet.common.exception.AppException;
import com.karina.smeet.common.exception.ErrorCode;
import com.karina.smeet.entity.postgre.Room;
import com.karina.smeet.entity.postgre.RoomMemberId;
import com.karina.smeet.entity.postgre.Roommember;
import com.karina.smeet.entity.postgre.User;
import com.karina.smeet.modules.friend.repository.FriendshipRepository;
import com.karina.smeet.modules.notification.facade.NotificationFacade;
import com.karina.smeet.modules.room.dto.request.CreateRoomRequest;
import com.karina.smeet.modules.room.dto.request.UpdateRoomRequest;
import com.karina.smeet.modules.room.dto.response.RoomDetailResponse;
import com.karina.smeet.modules.room.dto.response.RoomResponse;
import com.karina.smeet.modules.room.mapper.RoomMapper;
import com.karina.smeet.modules.room.repository.RoomMemberRepository;
import com.karina.smeet.modules.room.repository.RoomRepository;
import com.karina.smeet.modules.user.repository.UserRepository;
import com.karina.smeet.modules.user.service.OnlineStatusService;
import com.karina.smeet.entity.postgre.Roommember.Role;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)


public class RoomServiceImpl implements RoomService {
    RoomRepository roomRepository;
    RoomMemberRepository roomMemberRepository;
    UserRepository userRepository;
    OnlineStatusService onlineStatusService;
    FriendshipRepository friendshipRepository;
    RoomMapper roomMapper;
    NotificationFacade notificationFacade;

    @Override
    @Transactional
    public RoomResponse createRoom(UUID currentUserId, CreateRoomRequest request) {
        User creator = findUser(currentUserId);

        Room room = Room.builder()
                .type(Room.Type.GROUP)
                .name(request.name())
                .avatarUrl(request.avatarUrl())
                .createdBy(creator)
                .build();

        roomRepository.save(room);
        addMemberInterval(room, creator, Role.OWNER);

        for (UUID memberId : request.memberIds()) {
            if (memberId.equals(currentUserId)) continue;
            User member = findUser(memberId);
            addMemberInterval(room, member, Role.MEMBER);
        }

        return roomMapper.toRoomResponse(room, currentUserId);
    }

    @Override
    public List<RoomResponse> getMyRooms(UUID currentUserId, String filter) {
        List<RoomResponse> all = roomMapper.toRoomResponses(
                roomRepository.findAllRoomsByUserId(currentUserId), currentUserId);

        List<RoomResponse> sorted = all.stream()
                .sorted(Comparator.comparing(RoomResponse::lastMessageAt,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();

        return switch (filter) {
            case "group" -> sorted.stream().filter(r -> r.type() == Room.Type.GROUP).toList();
            case "unread" -> sorted.stream().filter(r -> r.unreadCount() > 0).toList();
            default -> sorted;
        };
    }

    @Override
    public RoomDetailResponse getRoomDetail(UUID roomId, UUID currentUserId) {
        Room room = findRoom(roomId);
        requireMember(roomId, currentUserId); //ensure the user is in the room to view the content

        return roomMapper.toRoomDetailResponse(room, currentUserId);
    }

    @Override
    @Transactional
    public void addMember(UUID roomId, UUID currentUserId, UUID targetUserId) {
        Room room = findRoom(roomId); //The group chat check has already been done.
        requireGroupRoom(room); //Check the room type must be 'group'


        //Verify that the person extending the invitation is already in the group and holds the role of admin or owner.
        Roommember actor = requireMember(roomId, currentUserId);
        requireAtLeastAdmin(actor);

        //The invited user must not already be in the group chat.
        if (roomMemberRepository.existsByRoom_IdAndUser_Id(roomId, targetUserId)) {
            throw new AppException(ErrorCode.ALREADY_ROOM_MEMBER);
        }

        User target = findUser(targetUserId);
        addMemberInterval(room, target, Role.MEMBER);

        notificationFacade.roomInvited(targetUserId, roomId, room.getName());
    }

    @Override
    @Transactional
    public void kickMember(UUID roomId, UUID currentUserId, UUID targetUserId) {
        if (currentUserId.equals(targetUserId)) {
            throw new AppException(ErrorCode.CANNOT_KICK_SELF);
        }

        Room room = findRoom(roomId);
        requireGroupRoom(room);

        Roommember actor = requireMember(roomId, currentUserId);
        Roommember target = requireMember(roomId, targetUserId);

        requireCanManage(actor, target);

        roomMemberRepository.delete(target);
    }

    @Override
    @Transactional
    public void leaveRoom(UUID roomId, UUID currentUserId) {
        Room room = findRoom(roomId);
        requireGroupRoom(room);

        Roommember me = requireMember(roomId, currentUserId);

        if (me.getRole() == Role.OWNER)
            throw new AppException(ErrorCode.OWNER_CANNOT_LEAVE);

        roomMemberRepository.delete(me);

        //long remaining = room
    }

    @Override
    @Transactional
    public RoomResponse updateRoom(UUID roomId, UUID currentUserId, UpdateRoomRequest request) {
        Room room = findRoom(roomId);
        requireGroupRoom(room);

        Roommember actor = requireMember(roomId, currentUserId);
        requireAtLeastAdmin(actor);

        if (request.name() != null && !request.name().isBlank())
            room.setName(request.name());

        if (request.avatarUrl() != null) {
            room.setAvatarUrl(request.avatarUrl());
        }

        roomRepository.save(room);
        return roomMapper.toRoomResponse(room, currentUserId);
    }

    @Override
    @Transactional
    public void updateMemberRole(UUID roomId, UUID currentUserId, UUID targetUserId, Role newRole) {
        Room room = findRoom(roomId);
        requireGroupRoom(room);

        Roommember actor = requireMember(roomId, currentUserId);
        if(actor.getRole() != Role.OWNER)
            throw new AppException(ErrorCode.PERMISSION_DENIED);

        if(currentUserId.equals(targetUserId))
            throw new AppException(ErrorCode.CANNOT_CHANGE_OWN_ROLE);

        Roommember target = requireMember(roomId, targetUserId);
        if (newRole == Role.OWNER) {
            actor.setRole(Role.ADMIN);
            target.setRole(Role.OWNER);
        } else {
            target.setRole(newRole);
        }

        try {
            if (newRole == Role.OWNER) {
                roomMemberRepository.saveAndFlush(actor);
            }
            roomMemberRepository.saveAndFlush(target);
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new AppException(ErrorCode.ROOM_MEMBER_CONFLICT);
        }
    }


    @Override
    @Transactional
    public void deleteRoom(UUID roomId, UUID currentUserId) {
        Room room = findRoom(roomId);
        requireGroupRoom(room);

        Roommember actor = requireMember(roomId, currentUserId);
        if(actor.getRole() != Role.OWNER)
            throw new AppException(ErrorCode.PERMISSION_DENIED);

        // Room already cascades (CascadeType.ALL + orphanRemoval) to its members association,
        // so deleting the room alone is enough — no need to delete members separately first.
        roomRepository.delete(room);

        //todo: we can call this later if needed:
        //messageRepository.deleteByRoomId(roomId) — clear chat history in MongoDB
        //or use soft-delete instead of hard-delete to keep an archive
    }

    private User findUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
    }

    private Room findRoom(UUID roomId) {
        return roomRepository.findById(roomId)
                .orElseThrow(() -> new AppException(ErrorCode.ROOM_NOT_FOUND));
    }

    private Roommember requireMember(UUID roomId, UUID userId) {
        return roomMemberRepository.findByRoom_IdAndUser_Id(roomId, userId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_ROOM_MEMBER));
    }

    private void requireGroupRoom(Room room) {
        if (room.getType() != Room.Type.GROUP) {
            throw new AppException(ErrorCode.CANNOT_MODIFY_DIRECT_ROOM);
        }
    }

    private void requireAtLeastAdmin(Roommember actor) {
        if (actor.getRole() == Role.MEMBER) {
            throw new AppException(ErrorCode.PERMISSION_DENIED);
        }
    }

    private void requireCanManage(Roommember actor, Roommember target) {
        if (actor.getRole() == Role.MEMBER) {
            throw new AppException(ErrorCode.PERMISSION_DENIED);
        }

        if (target.getRole() == Role.OWNER) {
            throw new AppException(ErrorCode.CANNOT_KICK_OWNER);
        }

        if (actor.getRole() == Role.ADMIN && target.getRole() != Role.MEMBER) {
            throw new AppException(ErrorCode.PERMISSION_DENIED);
        }
    }

    private void addMemberInterval(Room room, User user, Role role) {
        Roommember member = Roommember.builder()
                .id(new RoomMemberId(room.getId(), user.getId()))
                .room(room)
                .user(user)
                .role(role)
                .build();

        // saveAndFlush forces the unique-key violation to surface here (instead of at the
        // next unrelated flush) so a concurrent duplicate add is caught and translated below.
        try {
            roomMemberRepository.saveAndFlush(member);
        } catch (DataIntegrityViolationException e) {
            throw new AppException(ErrorCode.ALREADY_ROOM_MEMBER);
        }
    }
}
