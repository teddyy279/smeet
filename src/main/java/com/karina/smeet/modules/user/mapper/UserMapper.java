package com.karina.smeet.modules.user.mapper;


import com.karina.smeet.entity.postgre.User;
import com.karina.smeet.modules.friend.repository.FriendshipRepository;
import com.karina.smeet.modules.user.dto.response.MyProfileResponse;
import com.karina.smeet.modules.user.dto.response.UserProfileResponse;
import com.karina.smeet.modules.user.service.OnlineStatusService;
import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

@Mapper(componentModel = "spring")

public abstract class UserMapper {
    @Autowired
    protected FriendshipRepository friendshipRepository;

    @Autowired
    protected OnlineStatusService onlineStatusService;

    public abstract MyProfileResponse toMyProfile(User user);

    public UserProfileResponse toUserProfile(User target, UUID currentUserId) {
        return UserProfileResponse.builder()
                .id(target.getId())
                .username(target.getUsername())
                .displayName(target.getDisplayName())
                .avatarUrl(target.getAvatarUrl())
                .isOnline(onlineStatusService.isOnline(target.getId()))
                .relationshipStatus(getRelationshipStatus(currentUserId, target.getId()))
                .build();
    }


    private UserProfileResponse.RelationshipStatus getRelationshipStatus(
            UUID currentUserId, UUID targetId) {
        return friendshipRepository.findBetween(currentUserId, targetId)
                .map(f -> switch (f.getStatus()) {
                    case ACCEPTED -> UserProfileResponse.RelationshipStatus.FRIEND;
                    case PENDING -> f.getRequester().getId().equals(currentUserId)
                            ? UserProfileResponse.RelationshipStatus.PENDING_SENT
                            : UserProfileResponse.RelationshipStatus.PENDING_RECEIVED;
                    case REJECTED -> UserProfileResponse.RelationshipStatus.NONE;
                }).orElse(UserProfileResponse.RelationshipStatus.NONE);
    }
}
