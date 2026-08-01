package com.karina.smeet.modules.chat.controller;

import com.karina.smeet.common.response.ApiResponse;
import com.karina.smeet.modules.chat.dto.request.ForwardMessageRequest;
import com.karina.smeet.modules.chat.dto.response.ChatMessageResponse;
import com.karina.smeet.modules.chat.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor


public class ChatController {
    private final ChatService chatService;

    @GetMapping("/rooms/{roomId}/messages")
    public ApiResponse<List<ChatMessageResponse>> getHistory(
            @PathVariable String roomId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant before,
            @RequestParam(required = false) String beforeId,
            @RequestParam(defaultValue = "30") int size,
            @AuthenticationPrincipal UUID userId) {
        return ApiResponse.<List<ChatMessageResponse>>builder()
                .result(chatService.getHistory(roomId, userId, before, beforeId, size))
                .build();
    }

    @DeleteMapping("/messages/{messageId}")
    public ApiResponse<Void> deleteMessage(
            @PathVariable String messageId,
            @AuthenticationPrincipal UUID userId) {

        chatService.deleteMessage(messageId, userId);
        return ApiResponse.<Void>builder()
                .message("Message recalled!")
                .build();
    }

    @DeleteMapping("/messages/{messageId}/hide")
    public ApiResponse<Void> hideMessageForMe(
            @PathVariable String messageId,
            @AuthenticationPrincipal UUID userId) {

        chatService.hideMessageForMe(messageId, userId);
        return ApiResponse.<Void>builder()
                .message("Message deleted for you!")
                .build();
    }

    @PostMapping("/messages/{messageId}/forward")
    public ApiResponse<List<ChatMessageResponse>> forwardMessage(
            @PathVariable String messageId,
            @Valid @RequestBody ForwardMessageRequest request,
            @AuthenticationPrincipal UUID userId) {

        return ApiResponse.<List<ChatMessageResponse>>builder()
                .result(chatService.forwardMessage(messageId, userId, request.targetRoomIds()))
                .build();
    }

    @PostMapping("/rooms/{roomId}/pins/{messageId}")
    public ApiResponse<Void> pinMessage(
            @PathVariable String roomId,
            @PathVariable String messageId,
            @AuthenticationPrincipal UUID userId) {

        chatService.pinMessage(roomId, messageId, userId);
        return ApiResponse.<Void>builder()
                .message("Message pinned!")
                .build();
    }

    @DeleteMapping("/rooms/{roomId}/pins/{messageId}")
    public ApiResponse<Void> unpinMessage(
            @PathVariable String roomId,
            @PathVariable String messageId,
            @AuthenticationPrincipal UUID userId) {

        chatService.unpinMessage(roomId, messageId, userId);
        return ApiResponse.<Void>builder()
                .message("Message unpinned!")
                .build();
    }

    @GetMapping("/rooms/{roomId}/pins")
    public ApiResponse<List<ChatMessageResponse>> getPinnedMessages(
            @PathVariable String roomId,
            @AuthenticationPrincipal UUID userId) {

        return ApiResponse.<List<ChatMessageResponse>>builder()
                .result(chatService.getPinnedMessages(roomId, userId))
                .build();
    }

    @GetMapping("rooms/unread-count")
    public ApiResponse<Map<UUID, Long>> getUnreadCountByRoom(@AuthenticationPrincipal UUID userId) {
        return ApiResponse.<Map<UUID, Long>>builder()
                .result(chatService.getUnreadCountByRoom(userId))
                .build();
    }

}
