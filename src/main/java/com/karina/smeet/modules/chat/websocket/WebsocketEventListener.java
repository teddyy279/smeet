package com.karina.smeet.modules.chat.websocket;


import com.karina.smeet.modules.user.service.OnlineStatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j


public class WebsocketEventListener {
    private final OnlineStatusService onlineStatusService;

    @EventListener
    public void handleConnect(SessionConnectedEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        UUID userId = extractUserId(accessor);

        if (userId != null) {
            onlineStatusService.setOnline(userId);
            log.info("User connected via Websocket: {}", userId);
        }
    }

    @EventListener
    public void handleDisconnet(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        UUID userId = extractUserId(accessor);

        if (userId != null) {
            onlineStatusService.setOffline(userId);
            log.info("User disconnected from WebSocket: {}", userId);
        }
    }

    private UUID extractUserId(StompHeaderAccessor accessor) {
        Principal principal = accessor.getUser();
        if (principal == null) return null;
        try {
            return UUID.fromString(principal.getName());
        } catch (IllegalArgumentException exception){
            log.warn("Cannot parse userId from principal: {}", principal.getName());
            return null;
        }
    }
}
