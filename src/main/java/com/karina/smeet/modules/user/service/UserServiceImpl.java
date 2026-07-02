package com.karina.smeet.modules.user.service;

import com.karina.smeet.common.exception.AppException;
import com.karina.smeet.common.exception.ErrorCode;
import com.karina.smeet.entity.postgre.User;
import com.karina.smeet.entity.postgre.UserAuthProvider;
import com.karina.smeet.enums.Provider;
import com.karina.smeet.modules.auth.repository.UserAuthProviderRepository;
import com.karina.smeet.modules.user.dto.request.ChangePasswordRequest;
import com.karina.smeet.modules.user.dto.request.UpdateProfileRequest;
import com.karina.smeet.modules.user.dto.response.MyProfileResponse;
import com.karina.smeet.modules.user.dto.response.SearchResponse;
import com.karina.smeet.modules.user.dto.response.UserProfileResponse;
import com.karina.smeet.modules.user.mapper.UserMapper;
import com.karina.smeet.modules.user.repository.UserRepository;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Limit;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)


public class UserServiceImpl implements UserService{
    private static final int MIN_QUERY_LENGTH = 2;
    private static final Limit SEARCH_RESULT_LIMIT = Limit.of(20);

    AvatarService avatarService;
    UserRepository userRepository;
    PasswordEncoder passwordEncoder;
    UserMapper userMapper;
    UserAuthProviderRepository userAuthProviderRepository;
    OnlineStatusService onlineStatusService;
    RecentSearchService recentSearchService;

    @Override
    public MyProfileResponse getMyProfile(UUID userId) {
        User user = findUserById(userId);
        return userMapper.toMyProfile(user);
    }

    @Override
    @Transactional
    public MyProfileResponse updateProfile(UUID userId, UpdateProfileRequest request) {
        User user = findUserById(userId);
        user.setDisplayName(request.displayName());
        userRepository.save(user);
        return userMapper.toMyProfile(user);
    }

    @Override
    @Transactional
    public String updateAvatarUrl(UUID userId, String avatarUrl) {
        User user = findUserById(userId);
        //String avatarUrl = avatarService.uploadAvatar(userId, file);
        user.setAvatarUrl(avatarUrl);
        userRepository.save(user);
        return avatarUrl;
    }


    @Override
    @Transactional
    public void changePassword(UUID userId, ChangePasswordRequest request) {
        User user = findUserById(userId);

        UserAuthProvider localProvider = userAuthProviderRepository
                .findByUserAndProvider(user, Provider.LOCAL)
                .orElseThrow(() -> new AppException(ErrorCode.AUTH_PROVIDER_NOT_FOUND));

        if(!passwordEncoder.matches(request.currentPassword(), localProvider.getPasswordHash())) {
            throw new AppException(ErrorCode.INVALID_CREDENTIALS);
        }

        localProvider.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userAuthProviderRepository.save(localProvider);
    }

    @Override
    public UserProfileResponse getUserProfile(UUID targetId, UUID currentUserId) {
        User target = findUserById(targetId);
        return userMapper.toUserProfile(target, currentUserId);
    }

    @Override
    public SearchResponse search(String query, UUID currentUserId) {
        if (isTooShort(query)) {
            return emptySearchResponse();
        }
        List<User> friends = userRepository.searchFriends(
                query.trim(), currentUserId, SEARCH_RESULT_LIMIT);

        return SearchResponse.builder()
                .friends(toFriendItems(friends))
                .rooms(List.of())
                .build();
    }

    @Override
    public SearchResponse searchGlobal(String query, UUID currentUserId) {
        if (isTooShort(query)) {
            return emptySearchResponse();
        }
        List<User> users = userRepository.searchGlobal(
                query.trim(), currentUserId, SEARCH_RESULT_LIMIT);

        return SearchResponse.builder()
                .friends(toFriendItems(users))
                .rooms(List.of())
                .build();
    }

    private boolean isTooShort(String query) {
        return query == null || query.trim().length() < MIN_QUERY_LENGTH;
    }

    private SearchResponse emptySearchResponse() {
        return SearchResponse.builder()
                .friends(List.of())
                .rooms(List.of())
                .build();
    }

    private List<SearchResponse.FriendItem> toFriendItems(List<User> users) {
        return users.stream()
                .map(u -> SearchResponse.FriendItem.builder()
                        .userId(u.getId())
                        .username(u.getUsername())
                        .displayName(u.getDisplayName())
                        .avatarUrl(u.getAvatarUrl())
                        .isOnline(onlineStatusService.isOnline(u.getId()))
                        //.directRoomId
                        .build())
                .toList();
    }

    @Override
    public UserProfileResponse findByUsername(String username, UUID currentUserId) {
        User target = userRepository
                .findByUsernameExact(username, currentUserId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        recentSearchService.saveUser(currentUserId, target);
        return userMapper.toUserProfile(target, currentUserId);
    }

    @Override
    public void selectSearchResult(UUID userId, UUID currentUserId) {
        User selected = findUserById(currentUserId);

        recentSearchService.saveUser(currentUserId, selected);
    }

    @Override
    public void heartbeat(UUID userId) {
        // Gia hạn TTL → user vẫn online
        onlineStatusService.setOnline(userId);
    }

    private User findUserById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() ->  new AppException(ErrorCode.USER_NOT_EXISTED));
    }

}
