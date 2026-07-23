package com.karina.smeet.modules.chat.service;

import com.karina.smeet.common.exception.AppException;
import com.karina.smeet.common.exception.ErrorCode;
import com.karina.smeet.entity.mongo.Message;
import com.karina.smeet.entity.postgre.Roommember;
import com.karina.smeet.entity.postgre.User;
import com.karina.smeet.modules.chat.dto.request.SendMessageRequest;
import com.karina.smeet.modules.chat.dto.response.ChatMessageResponse;
import com.karina.smeet.modules.chat.repository.MessageRepository;
import com.karina.smeet.modules.notification.facade.NotificationFacade;
import com.karina.smeet.modules.room.repository.RoomMemberRepository;
import com.karina.smeet.modules.user.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)

public class ChatServiceImpl implements ChatService{
    MessageRepository messageRepository;
    RoomMemberRepository roomMemberRepository;
    UserRepository userRepository;
    ActiveRoomService activeRoomService;
    NotificationFacade notificationFacade;
    SimpMessagingTemplate simpMessagingTemplate;

    @Override
    @Transactional
    public ChatMessageResponse sendMessage(String roomId, UUID senderId, SendMessageRequest request) {
        UUID roomUuid = UUID.fromString(roomId);

        Roommember senderMembership = roomMemberRepository
                .findByRoom_IdAndUser_Id(roomUuid, senderId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_ROOM_MEMBER));

        User sender = senderMembership.getUser();

        Message message = Message.builder()
                .roomId(roomId)
                .senderId(senderId.toString())
                .senderName(sender.getDisplayName())
                .senderAvatar(sender.getAvatarUrl())
                .type(request.type())
                .content(request.content())
                .mediaUrls(request.mediaUrls() != null ? request.mediaUrls() : List.of())
                .durationSeconds(request.durationSeconds())
                .replyTo(request.replyTo())
                .build();

        Message saved = messageRepository.save(message);

        senderMembership.setLastReadAt(Instant.now());
        roomMemberRepository.save(senderMembership);

        ChatMessageResponse response = toResponse(saved);


        //broadcast to all members who are subscribed /topic/room.{roomId}
        simpMessagingTemplate.convertAndSend("/topic/room." + roomId, response);

        List<Roommember> members = roomMemberRepository.findByRoom_Id(roomUuid);
        for(Roommember member : members) {
            UUID memberId = member.getUser().getId();
            if (memberId.equals(senderId)) continue;
            if (!activeRoomService.isViewingRoom(memberId, roomId)) {
                notificationFacade.missedMessage(memberId, roomId, sender.getDisplayName());
            }
        }
        return response;
    }

    @Override
    @Transactional
    public void markAsRead(String roomId, UUID userId) {
        UUID roomUuid = UUID.fromString(roomId);
        Roommember membership = roomMemberRepository
                .findByRoom_IdAndUser_Id(roomUuid, userId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_ROOM_MEMBER));

        membership.setLastReadAt(Instant.now());
        roomMemberRepository.save(membership);

        simpMessagingTemplate.convertAndSend(
                "/topic/room." + roomId + ".seen",
                new SeenEvent(userId.toString(), Instant.now())
        );
    }

    private record SeenEvent(String userId, Instant seenAt) {}

    @Override
    @Transactional
    public ChatMessageResponse addReaction(String messageId, UUID userId, String emoji) {
        Message message = messageRepository.findByIdAndDeletedAtIsNull(messageId)
                .orElseThrow(() -> new AppException(ErrorCode.MESSAGE_NOT_FOUND));

        UUID roomId = UUID.fromString(message.getRoomId());
        roomMemberRepository.findByRoom_IdAndUser_Id(roomId, userId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_ROOM_MEMBER));

        List<Message.Reaction> reactions = message.getReactions();
        reactions.removeIf(r -> r.getUserId().equals(userId.toString()));
        reactions.add(new Message.Reaction(userId.toString(), emoji));

        Message saved = messageRepository.save(message);
        ChatMessageResponse response = toResponse(saved);

        simpMessagingTemplate.convertAndSend("/topic/room" + message.getRoomId(), response);

        return response;
    }

    @Override
    @Transactional
    public void deleteMessage(String messageId, UUID userId) {
        Message message = messageRepository.findByIdAndDeletedAtIsNull(messageId)
                .orElseThrow(() -> new AppException(ErrorCode.MESSAGE_NOT_FOUND));

        if (!message.getSenderId().equals(userId.toString()))
            throw new AppException(ErrorCode.CANNOT_DELETE_MESSAGE);

        message.setDeletedAt(Instant.now());
        messageRepository.save(message);

        simpMessagingTemplate.convertAndSend(
                "/topic/room." + message.getRoomId() + ".deleted",
                messageId
        );
    }

    @Override
    public List<ChatMessageResponse> getHistory(String roomId, UUID userId, Instant before, String beforeId, int size) {
        UUID roomUuid = UUID.fromString(roomId);

        roomMemberRepository.findByRoom_IdAndUser_Id(roomUuid, userId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_ROOM_MEMBER));

        Pageable pageable = PageRequest.of(0, size);

        List<Message> messages = (before == null || beforeId == null)
                ? messageRepository.findByRoomIdAndDeletedAtIsNullOrderByCreatedAtDescIdDesc(roomId, pageable)
                : messageRepository.findOlderThanCursor(roomId, before, beforeId, pageable);

        return messages.stream().map(this::toResponse).toList();
    }

    private ChatMessageResponse toResponse(Message m) {
        return ChatMessageResponse.builder()
                .id(m.getId())
                .roomId(m.getRoomId())
                .senderId(m.getSenderId())
                .senderName(m.getSenderName())
                .senderAvatar(m.getSenderAvatar())
                .type(m.getType())
                .content(m.getContent())
                .mediaUrls(m.getMediaUrls())
                .durationSeconds(m.getDurationSeconds())
                .replyTo(m.getReplyTo())
                .reactions(m.getReactions().stream()
                        .map(r -> ChatMessageResponse.ReactionDto.builder()
                                .userId(r.getUserId())
                                .emoji(r.getEmoji())
                                .build())
                        .toList())
                .seenBy(m.getSeenBy().stream()
                        .map(s -> ChatMessageResponse.SeenByDto.builder()
                                .userId(s.getUserId())
                                .seenAt(s.getSeenAt())
                                .build())
                        .toList())
                .createdAt(m.getCreatedAt())
                .build();
    }
}
