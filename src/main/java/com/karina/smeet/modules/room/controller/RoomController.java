package com.karina.smeet.modules.room.controller;

import com.karina.smeet.common.response.ApiResponse;
import com.karina.smeet.modules.room.dto.request.AddMemberRequest;
import com.karina.smeet.modules.room.dto.request.CreateRoomRequest;
import com.karina.smeet.modules.room.dto.request.UpdateRoleRequest;
import com.karina.smeet.modules.room.dto.request.UpdateRoomRequest;
import com.karina.smeet.modules.room.dto.response.RoomDetailResponse;
import com.karina.smeet.modules.room.dto.response.RoomResponse;
import com.karina.smeet.modules.room.service.RoomService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/rooms")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)

public class RoomController {
    RoomService roomService;

    @PostMapping
    public ApiResponse<RoomResponse> createRoom(
            @Valid @RequestBody CreateRoomRequest request,
            @AuthenticationPrincipal UUID currentUserId) {
        return ApiResponse.<RoomResponse>builder()
                .result(roomService.createRoom(currentUserId, request))
                .build();
    }

    @GetMapping
    public ApiResponse<List<RoomResponse>> getMyGroupRooms(
            @AuthenticationPrincipal UUID currentUserId) {
        return ApiResponse.<List<RoomResponse>>builder()
                .result(roomService.getMyGroupRooms(currentUserId))
                .build();
    }

    @GetMapping("/{roomId}")
    public ApiResponse<RoomDetailResponse> getRoomDetail(
            @PathVariable UUID roomId,
            @AuthenticationPrincipal UUID currentUserId) {
        return ApiResponse.<RoomDetailResponse>builder()
                .result(roomService.getRoomDetail(roomId, currentUserId))
                .build();
    }

    @PatchMapping("/{roomId}")
    public ApiResponse<RoomResponse> updateRoom(
            @PathVariable UUID roomId,
            @Valid @RequestBody UpdateRoomRequest request,
            @AuthenticationPrincipal UUID currentUserId) {
        return ApiResponse.<RoomResponse>builder()
                .result(roomService.updateRoom(roomId, currentUserId, request))
                .build();
    }

    @DeleteMapping("/{roomId}")
    public ApiResponse<Void> deleteRoom(
            @PathVariable UUID roomId,
            @AuthenticationPrincipal UUID currentUserId) {
        roomService.deleteRoom(roomId, currentUserId);
        return ApiResponse.<Void>builder()
                .message("Room deleted successfully!")
                .build();
    }

    @PostMapping("/{roomId}/members")
    public ApiResponse<Void> addMember(
            @PathVariable UUID roomId,
            @Valid @RequestBody AddMemberRequest request,
            @AuthenticationPrincipal UUID currentUserId) {
        roomService.addMember(roomId, currentUserId, request.userId());
        return ApiResponse.<Void>builder()
                .message("Member added successfully!")
                .build();
    }

    @DeleteMapping("/{roomId}/members/{targetUserId}")
    public ApiResponse<Void> kickMember(
            @PathVariable UUID roomId,
            @PathVariable UUID targetUserId,
            @AuthenticationPrincipal UUID currentUserId) {
        roomService.kickMember(roomId, currentUserId, targetUserId);
        return ApiResponse.<Void>builder()
                .message("Member removed successfully!")
                .build();
    }

    @PatchMapping("/{roomId}/members/{targetUserId}/role")
    public ApiResponse<Void> updateMemberRole(
            @PathVariable UUID roomId,
            @PathVariable UUID targetUserId,
            @Valid @RequestBody UpdateRoleRequest request,
            @AuthenticationPrincipal UUID currentUserId) {
        roomService.updateMemberRole(roomId, currentUserId, targetUserId, request.role());
        return ApiResponse.<Void>builder()
                .message("Member role updated successfully!")
                .build();
    }

    @PostMapping("/{roomId}/leave")
    public ApiResponse<Void> leaveRoom(
            @PathVariable UUID roomId,
            @AuthenticationPrincipal UUID currentUserId) {
        roomService.leaveRoom(roomId, currentUserId);
        return ApiResponse.<Void>builder()
                .message("You have left the room!")
                .build();
    }
}
