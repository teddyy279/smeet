package com.karina.smeet.modules.friend.service;

import com.karina.smeet.modules.friend.dto.response.FriendRequestResponse;
import com.karina.smeet.modules.friend.dto.response.FriendResponse;

import java.util.List;
import java.util.UUID;

public interface FriendService {
    void sendFriendRequest(UUID currentUserId, UUID targetUserId);

    void acceptFriendRequest(UUID friendshipId, UUID currentUserId);

    void rejectFriendRequest(UUID friendshipId, UUID currentUserId);

    void unfriend(UUID currentUserId, UUID targetUserId);

    List<FriendResponse> getFriends(UUID userId);

    List<FriendRequestResponse> getPendingRequests(UUID userId);

    List<FriendRequestResponse> getSentRequests(UUID userId);
}
