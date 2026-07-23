package com.karina.smeet.modules.chat.controller;

import com.karina.smeet.modules.user.service.OnlineStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.UUID;

@Controller
@RequiredArgsConstructor

public class PresenceController {
    private final OnlineStatusService onlineStatusService;

    @MessageMapping("/presence.ping")
    public void pring(Principal principal) {
        UUID userId = UUID.fromString(principal.getName());
        onlineStatusService.setOnline(userId);
    }
}
