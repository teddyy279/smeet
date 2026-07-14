package com.karina.smeet.modules.friend.controller;

import com.karina.smeet.common.response.ApiResponse;
import com.karina.smeet.modules.friend.dto.response.FriendRequestResponse;
import com.karina.smeet.modules.friend.dto.response.FriendResponse;
import com.karina.smeet.modules.friend.service.FriendService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/friends")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)

public class FriendController {
    FriendService friendService;

    @GetMapping
    public ApiResponse<List<FriendResponse>> getFriends(
            @AuthenticationPrincipal UUID currentUserId) {
        return ApiResponse.<List<FriendResponse>>builder()
                .result(friendService.getFriends(currentUserId))
                .build();
    }

    @PostMapping("/request/{targetUserId}")
    public ApiResponse<Void> sendRequest(
            @PathVariable UUID targetUserId,
            @AuthenticationPrincipal UUID currentUserId) {
        friendService.sendFriendRequest(currentUserId, targetUserId);
        return ApiResponse.<Void>builder()
                .message("The friend request has been sent!")
                .build();
    }

    @GetMapping("/request/pending")
    public ApiResponse<List<FriendRequestResponse>> getPendingRequests(
            @AuthenticationPrincipal UUID currentUserId) {
        return ApiResponse.<List<FriendRequestResponse>>builder()
                .result(friendService.getPendingRequests(currentUserId))
                .build();
    }

    @GetMapping("/request/sent")
    public ApiResponse<List<FriendRequestResponse>> getSentRequest(
            @AuthenticationPrincipal UUID currentUserId) {
        return ApiResponse.<List<FriendRequestResponse>>builder()
                .result(friendService.getSentRequests(currentUserId))
                .build();
    }

    @PostMapping("/request/{friendshipId}/accept")
    public ApiResponse<Void> acceptRequest(
            @PathVariable UUID friendshipId,
            @AuthenticationPrincipal UUID currentUserId) {
        friendService.acceptFriendRequest(friendshipId, currentUserId);
        return ApiResponse.<Void>builder()
                .message("Friend request accepted successfully!")
                .build();
    }

    @PostMapping("/request/{friendshipId}/reject")
    public ApiResponse<Void> rejectRequest(
            @PathVariable UUID friendshipId,
            @AuthenticationPrincipal UUID currentUserId) {
        friendService.rejectFriendRequest(friendshipId, currentUserId);
        return ApiResponse.<Void>builder()
                .message("Declined the friend request!")
                .build();
    }

    @DeleteMapping("/{targetUserId}")
    public ApiResponse<Void> unfriend(
            @PathVariable UUID targetUserId,
            @AuthenticationPrincipal UUID currentUserId) {
        friendService.unfriend(currentUserId, targetUserId);
        return ApiResponse.<Void>builder()
                .message("Unfriended successfully!")
                .build();
    }
}
