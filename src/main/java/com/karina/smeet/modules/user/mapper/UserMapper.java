package com.karina.smeet.modules.user.mapper;


import com.karina.smeet.entity.postgre.User;
import com.karina.smeet.modules.user.dto.response.MyProfileResponse;
import com.karina.smeet.modules.user.dto.response.UserProfileResponse;
import org.mapstruct.Mapper;

import java.util.UUID;

@Mapper(componentModel = "spring")

public interface UserMapper {
    MyProfileResponse toMyProfile(User user);
    UserProfileResponse toUserProfile(User target, UUID currentUserId);
}
