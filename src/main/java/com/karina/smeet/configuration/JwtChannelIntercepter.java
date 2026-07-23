package com.karina.smeet.configuration;


import com.karina.smeet.common.utils.JwtUtil;
import io.jsonwebtoken.lang.Collections;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.security.Principal;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j

public class JwtChannelIntercepter implements ChannelInterceptor {
    private final JwtUtil jwtUtil;

    @Override
    public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String token = accessor.getFirstNativeHeader("Authorization");

            if (!StringUtils.hasText(token) || !token.startsWith("Bearer ")) {
                log.warn("STOMP CONNECT rejected: missing Authorization header");
                throw new IllegalArgumentException("Missing or invalid Authorization header");
            }

            String jwt = token.substring("Bearer ".length());

            if (!jwtUtil.isValid(jwt)) {
                log.warn("STOMP CONNECT rejected: invalid JWT");
                throw new IllegalArgumentException("Invalid JWT token");
            }

            UUID userId = jwtUtil.extractUserId(jwt);

            Principal principal = new UsernamePasswordAuthenticationToken(
                    userId.toString(), null, Collections.emptyList()
            );

            accessor.setUser(principal);
        }
        return message;
    }
}
