package com.karina.smeet.modules.chat.controller;

import com.karina.smeet.modules.chat.dto.request.MarkReadRequest;
import com.karina.smeet.modules.chat.dto.request.ReactionRequest;
import com.karina.smeet.modules.chat.dto.request.SendMessageRequest;
import com.karina.smeet.modules.chat.service.ActiveRoomService;
import com.karina.smeet.modules.chat.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.UUID;

@Controller
@RequiredArgsConstructor

public class ChatWsController {
    private final ChatService chatService;
    private final ActiveRoomService activeRoomService;

    @MessageMapping("/chat.send")
    public void send(@Valid @Payload SendMessageRequest request, Principal principal) {
        UUID senderId = UUID.fromString(principal.getName());
        chatService.sendMessage(request.roomId(), senderId, request);
    }

    @MessageMapping("/chat.markRead")
    public void markRead(@Valid @Payload MarkReadRequest request, Principal principal) {
        UUID userId = UUID.fromString(principal.getName());
        chatService.markAsRead(request.roomId(), userId);
    }

    @MessageMapping("/chat.reaction")
    public void react(@Valid @Payload ReactionRequest request, Principal principal) {
        UUID userId = UUID.fromString(principal.getName());
        chatService.addReaction(request.messageId(), userId, request.emoji());
    }

    @MessageMapping("/chat.enterRoom/{roomId}")
    public void enterRoom(@DestinationVariable String roomId, Principal principal) {
        UUID userId = UUID.fromString(principal.getName());
        activeRoomService.enterRoom(userId, roomId);
    }

    @MessageMapping("/chat.leaveRoom")
    public void leaveRoom(Principal principal) {
        UUID userId = UUID.fromString(principal.getName());
        activeRoomService.leaveRoom(userId);
    }
}
