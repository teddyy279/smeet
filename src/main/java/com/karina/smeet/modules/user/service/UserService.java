package com.karina.smeet.modules.user.service;

import com.karina.smeet.modules.user.dto.request.ChangePasswordRequest;
import com.karina.smeet.modules.user.dto.request.UpdateProfileRequest;
import com.karina.smeet.modules.user.dto.response.MyProfileResponse;
import com.karina.smeet.modules.user.dto.response.SearchResponse;
import com.karina.smeet.modules.user.dto.response.UserProfileResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface UserService {
    MyProfileResponse getMyProfile(UUID userId);
    MyProfileResponse updateProfile(UUID userId, UpdateProfileRequest request);

    String updateAvatarUrl(UUID userId, String avatarUrl);

    void changePassword(UUID userId, ChangePasswordRequest request);

    UserProfileResponse getUserProfile(UUID targetId, UUID currentUserId);

    SearchResponse search(String query, UUID currentUserId);

    UserProfileResponse findByUsername(String username, UUID currentUserId);

    void selectSearchResult(UUID currentUserId, UUID selectedUserId);

    void heartbeat(UUID userId);
    //UserProfileResponse
}
