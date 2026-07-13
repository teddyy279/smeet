package com.karina.smeet.modules.friend.service;

import com.karina.smeet.common.exception.AppException;
import com.karina.smeet.common.exception.ErrorCode;
import com.karina.smeet.entity.postgre.*;
import com.karina.smeet.modules.friend.dto.response.FriendRequestResponse;
import com.karina.smeet.modules.friend.dto.response.FriendResponse;
import com.karina.smeet.modules.friend.repository.FriendshipRepository;
import com.karina.smeet.modules.notification.facade.NotificationFacade;
import com.karina.smeet.modules.room.repository.RoomMemberRepository;
import com.karina.smeet.modules.room.repository.RoomRepository;
import com.karina.smeet.modules.user.repository.UserRepository;
import com.karina.smeet.modules.user.service.OnlineStatusService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)

public class FriendServiceImpl implements  FriendService{
    FriendshipRepository friendshipRepository;
    RoomMemberRepository roomMemberRepository;
    RoomRepository roomRepository;
    UserRepository userRepository;
    NotificationFacade notificationFacade;
    OnlineStatusService onlineStatusService;

    @Override
    @Transactional
    public void sendFriendRequest(UUID currentUserId, UUID targetUserId) {
        if (currentUserId.equals(targetUserId))
            throw new AppException(ErrorCode.CANNOT_ADD_SELF);


        User requester = findUserById(currentUserId);
        User addressee = findUserById(targetUserId);


        // If a friendship record exists, its status is already one of PENDING, ACCEPTED, or REJECTED.
        // Otherwise, no relationship has been created yet, so no record exists in the friendship table.
        friendshipRepository.findBetween(currentUserId, targetUserId)
                .ifPresent(f -> {
                    throw new AppException(switch (f.getStatus()){
                        case PENDING -> ErrorCode.FRIEND_REQUEST_ALREADY_SENT;
                        case ACCEPTED -> ErrorCode.ALREADY_FRIENDS;
                        case REJECTED -> ErrorCode.FRIEND_REQUEST_ALREADY_SENT;
                    });
                });

        Friendship friendship = Friendship.builder()
                .requester(requester)
                .addressee(addressee)
                .status(Friendship.Status.PENDING)
                .build();

        try {
            friendshipRepository.saveAndFlush(friendship);
        } catch (DataIntegrityViolationException e) {
            throw new AppException(ErrorCode.FRIENDSHIP_CONFLICT);
        }

        //notify to addressee
        notificationFacade.friendRequestSent(targetUserId, requester);
    }


    @Override
    @Transactional
    public void acceptFriendRequest(UUID friendshipId, UUID currentUserId) {
        Friendship friendship = findFriendshipById(friendshipId);

        if (!friendship.getAddressee().getId().equals(currentUserId))
            throw new AppException(ErrorCode.PERMISSION_DENIED);

        if (friendship.getStatus() != Friendship.Status.PENDING)
            throw new AppException(ErrorCode.INVALID_FRIENDSHIP_STATUS);

        friendship.setStatus(Friendship.Status.ACCEPTED);
        try {
            friendshipRepository.saveAndFlush(friendship);
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new AppException(ErrorCode.INVALID_FRIENDSHIP_STATUS);
        }

        Room directRoom = roomRepository
                .findDirectRoom(friendship.getRequester().getId(), friendship.getAddressee().getId())
                .orElseGet(() -> createDirectRoom(friendship.getRequester(), friendship.getAddressee()));

        notificationFacade.friendRequestAccepted(
                friendship.getRequester().getId(),
                friendship.getAddressee()
        );
    }


    @Override
    @Transactional
    public void rejectFriendRequest(UUID friendshipId, UUID currentUserId) {
        Friendship friendship = findFriendshipById(friendshipId);

        if (!friendship.getAddressee().getId().equals(currentUserId))
            throw new AppException(ErrorCode.PERMISSION_DENIED);

        if (friendship.getStatus() != Friendship.Status.PENDING)
            throw new AppException(ErrorCode.INVALID_FRIENDSHIP_STATUS);

        friendship.setStatus(Friendship.Status.REJECTED);
        try {
            friendshipRepository.saveAndFlush(friendship);
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new AppException(ErrorCode.INVALID_FRIENDSHIP_STATUS);
        }
    }

    @Override
    @Transactional
    public void unfriend(UUID currentUserId, UUID targetUserID) {
        Friendship friendship = friendshipRepository
                .findBetween(currentUserId, targetUserID)
                .orElseThrow(() -> new AppException(ErrorCode.FRIENDSHIP_NOT_FOUND));

        if (friendship.getStatus() != Friendship.Status.ACCEPTED)
            throw new AppException(ErrorCode.FRIENDSHIP_NOT_FOUND);

        friendshipRepository.delete(friendship);
    }


    @Override
    public List<FriendResponse> getFriends(UUID userId) {
        return userRepository.findAllFriends(userId).stream()
                .map(friend -> {
                    UUID directRoomId = roomRepository
                            .findDirectRoom(userId, friend.getId())
                            .map(Room::getId)
                            .orElse(null);

                    return FriendResponse.builder()
                            .userId(friend.getId())
                            .username(friend.getUsername())
                            .displayName(friend.getDisplayName())
                            .avatarUrl(friend.getAvatarUrl())
                            .isOnline(onlineStatusService.isOnline(friend.getId()))
                            .directRoomId(directRoomId)
                            .build();
                }).toList();
    }


    //invitation pending
    @Override
    public List<FriendRequestResponse> getPendingRequests(UUID userId) {
        return friendshipRepository.findPendingReceived(userId).stream()
                .map(this::toFriendRequestResponse)
                .toList();
    }


    //invitation sent
    @Override
    public List<FriendRequestResponse> getSentRequests(UUID userId) {
        return friendshipRepository.findPendingSent(userId).stream()
                .map(this::toFriendRequestResponse)
                .toList();
    }

    private Room createDirectRoom(User userA, User userB) {
        Room room = Room.builder()
                .type(Room.Type.DIRECT)
                .build();
        roomRepository.save(room);

        Roommember memberA = Roommember.builder()
                .id(new RoomMemberId(room.getId(), userA.getId()))
                .room(room)
                .user(userA)
                .role(Roommember.Role.MEMBER)
                .build();
        roomMemberRepository.save(memberA);

        Roommember memberB = Roommember.builder()
                .id(new RoomMemberId(room.getId(), userB.getId()))
                .room(room)
                .user(userB)
                .role(Roommember.Role.MEMBER)
                .build();

        roomMemberRepository.save(memberB);

        return room;
    }

    private User findUserById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
    }

    private Friendship findFriendshipById(UUID friendshipId) {
        return friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new AppException(ErrorCode.FRIENDSHIP_NOT_FOUND));
    }

    private FriendRequestResponse toFriendRequestResponse(Friendship f) {
        User requester = f.getRequester();
        return FriendRequestResponse.builder()
                .friendshipId(UUID.fromString(f.getId()))
                .requesterId(requester.getId())
                .requesterUsername(requester.getUsername())
                .requesterDisplayname(requester.getDisplayName())
                .requesterAvatarUrl(requester.getAvatarUrl())
                .status(f.getStatus())
                .createdAt(f.getCreatedAt())
                .build();
    }
}
