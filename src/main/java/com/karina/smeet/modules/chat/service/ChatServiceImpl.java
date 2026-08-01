package com.karina.smeet.modules.chat.service;

import com.karina.smeet.common.exception.AppException;
import com.karina.smeet.common.exception.ErrorCode;
import com.karina.smeet.entity.mongo.Message;
import com.karina.smeet.entity.postgre.Room;
import com.karina.smeet.entity.postgre.RoomPinnedMessage;
import com.karina.smeet.entity.postgre.Roommember;
import com.karina.smeet.entity.postgre.User;
import com.karina.smeet.modules.chat.dto.request.SendMessageRequest;
import com.karina.smeet.modules.chat.dto.response.ChatMessageResponse;
import com.karina.smeet.modules.chat.repository.MessageRepository;
import com.karina.smeet.modules.notification.facade.NotificationFacade;
import com.karina.smeet.modules.room.repository.RoomMemberRepository;
import com.karina.smeet.modules.room.repository.RoomPinnedMessageRepository;
import com.karina.smeet.modules.user.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)

public class ChatServiceImpl implements ChatService{
    private static final int MAX_PINNED_PER_ROOM = 5;

    MessageRepository messageRepository;
    RoomMemberRepository roomMemberRepository;
    RoomPinnedMessageRepository roomPinnedMessageRepository;
    UserRepository userRepository;
    ActiveRoomService activeRoomService;
    NotificationFacade notificationFacade;
    SimpMessagingTemplate simpMessagingTemplate;

    // helper dùng chung: throw NOT_ROOM_MEMBER nếu user không thuộc room
    private Roommember requireMembership(UUID roomUuid, UUID userId) {
        return roomMemberRepository.findByRoom_IdAndUser_Id(roomUuid, userId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_ROOM_MEMBER));
    }

    // helper dùng chung: lấy message ở bất kỳ trạng thái nào (kể cả đã thu hồi)
    private Message requireMessage(String messageId) {
        return messageRepository.findById(messageId)
                .orElseThrow(() -> new AppException(ErrorCode.MESSAGE_NOT_FOUND));
    }

    // helper dùng chung: lấy message, throw nếu đã bị thu hồi — dùng cho action chỉ áp dụng
    // trên tin nhắn còn "sống" (react, recall)
    private Message requireActiveMessage(String messageId) {
        return messageRepository.findByIdAndDeletedAtIsNull(messageId)
                .orElseThrow(() -> new AppException(ErrorCode.MESSAGE_NOT_FOUND));
    }

    // helper dùng chung: lấy message và xác nhận nó thuộc đúng roomId truyền vào
    private Message requireMessageInRoom(String messageId, String roomId) {
        Message message = requireMessage(messageId);
        if (!message.getRoomId().equals(roomId))
            throw new AppException(ErrorCode.MESSAGE_NOT_FOUND);
        return message;
    }

    @Override
    @Transactional
    public ChatMessageResponse sendMessage(String roomId, UUID senderId, SendMessageRequest request) {
        UUID roomUuid = UUID.fromString(roomId);

        Roommember senderMembership = requireMembership(roomUuid, senderId);

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

        return persistAndBroadcast(message, senderId, senderMembership, sender);
    }

    // lưu message, cập nhật lastReadAt của sender, broadcast realtime và bắn missed-message
    // notification cho các member không đang mở room — dùng chung cho sendMessage & forwardMessage
    private ChatMessageResponse persistAndBroadcast(
            Message message, UUID senderId, Roommember senderMembership, User sender) {
        Message saved = messageRepository.save(message);

        senderMembership.setLastReadAt(Instant.now());
        roomMemberRepository.save(senderMembership);

        ChatMessageResponse response = toResponse(saved);

        String roomId = saved.getRoomId();

        //broadcast to all members who are subscribed /topic/room.{roomId}
        simpMessagingTemplate.convertAndSend("/topic/room." + roomId, response);

        List<Roommember> members = roomMemberRepository.findByRoom_Id(UUID.fromString(roomId));
        for(Roommember member : members) {
            UUID memberId = member.getUser().getId();
            if (memberId.equals(senderId)) continue;
            if (!activeRoomService.isViewingRoom(memberId, roomId)) {
                notificationFacade.missedMessage(memberId, roomId, sender.getDisplayName(), previewOf(saved));
            }
        }
        return response;
    }

    private String previewOf(Message message) {
        return switch (message.getType()) {
            case TEXT -> message.getContent();
            case IMAGE -> "[Hình ảnh]";
            case VIDEO -> "[Video]";
            case FILE -> "[Tệp đính kèm]";
            case AUDIO -> "[Tin nhắn thoại]";
            case CALL -> "[Cuộc gọi]";
        };
    }

    @Override
    @Transactional
    public void markAsRead(String roomId, UUID userId) {
        UUID roomUuid = UUID.fromString(roomId);
        Roommember membership = requireMembership(roomUuid, userId);

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
        Message message = requireActiveMessage(messageId);
        requireMembership(UUID.fromString(message.getRoomId()), userId);

        List<Message.Reaction> reactions = message.getReactions();
        reactions.removeIf(r -> r.getUserId().equals(userId.toString()));
        reactions.add(new Message.Reaction(userId.toString(), emoji));

        Message saved = messageRepository.save(message);
        ChatMessageResponse response = toResponse(saved);

        simpMessagingTemplate.convertAndSend("/topic/room." + message.getRoomId(), response);

        return response;
    }

    @Override
    @Transactional
    public void deleteMessage(String messageId, UUID userId) {
        Message message = requireActiveMessage(messageId);

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
    @Transactional
    public void hideMessageForMe(String messageId, UUID userId) {
        Message message = requireMessage(messageId);
        requireMembership(UUID.fromString(message.getRoomId()), userId);

        String uid = userId.toString();
        if (!message.getDeletedFor().contains(uid)) {
            message.getDeletedFor().add(uid);
            messageRepository.save(message);
        }

        // đồng bộ các thiết bị khác của chính user này, không broadcast cho ai khác trong room
        simpMessagingTemplate.convertAndSendToUser(uid, "/queue/messages.hidden", messageId);
    }

    @Override
    @Transactional
    public List<ChatMessageResponse> forwardMessage(String messageId, UUID senderId, List<String> targetRoomIds) {
        Message source = requireMessage(messageId);

        if (source.getDeletedAt() != null)
            throw new AppException(ErrorCode.CANNOT_FORWARD_RECALLED);

        List<ChatMessageResponse> responses = new ArrayList<>();
        for (String targetRoomId : targetRoomIds) {
            Roommember senderMembership = requireMembership(UUID.fromString(targetRoomId), senderId);

            User sender = senderMembership.getUser();

            Message forwarded = Message.builder()
                    .roomId(targetRoomId)
                    .senderId(senderId.toString())
                    .senderName(sender.getDisplayName())
                    .senderAvatar(sender.getAvatarUrl())
                    .type(source.getType())
                    .content(source.getContent())
                    .mediaUrls(source.getMediaUrls())
                    .durationSeconds(source.getDurationSeconds())
                    .forwardedFrom(messageId)
                    .build();

            responses.add(persistAndBroadcast(forwarded, senderId, senderMembership, sender));
        }
        return responses;
    }

    @Override
    @Transactional
    public void pinMessage(String roomId, String messageId, UUID userId) {
        UUID roomUuid = UUID.fromString(roomId);

        Roommember membership = requireMembership(roomUuid, userId);
        requireMessageInRoom(messageId, roomId);

        if (roomPinnedMessageRepository.existsByRoom_IdAndMessageId(roomUuid, messageId))
            throw new AppException(ErrorCode.MESSAGE_ALREADY_PINNED);

        if (roomPinnedMessageRepository.countByRoom_Id(roomUuid) >= MAX_PINNED_PER_ROOM)
            throw new AppException(ErrorCode.PIN_LIMIT_EXCEEDED);

        RoomPinnedMessage pin = RoomPinnedMessage.builder()
                .room(membership.getRoom())
                .messageId(messageId)
                .pinnedBy(membership.getUser())
                .build();
        roomPinnedMessageRepository.save(pin);

        simpMessagingTemplate.convertAndSend(
                "/topic/room." + roomId + ".pinned",
                new PinEvent(messageId, "PINNED")
        );
    }

    @Override
    @Transactional
    public void unpinMessage(String roomId, String messageId, UUID userId) {
        UUID roomUuid = UUID.fromString(roomId);

        requireMembership(roomUuid, userId);

        RoomPinnedMessage pin = roomPinnedMessageRepository.findByRoom_IdAndMessageId(roomUuid, messageId)
                .orElseThrow(() -> new AppException(ErrorCode.MESSAGE_NOT_PINNED));

        roomPinnedMessageRepository.delete(pin);

        simpMessagingTemplate.convertAndSend(
                "/topic/room." + roomId + ".pinned",
                new PinEvent(messageId, "UNPINNED")
        );
    }

    private record PinEvent(String messageId, String action) {}

    @Override
    public List<ChatMessageResponse> getPinnedMessages(String roomId, UUID userId) {
        UUID roomUuid = UUID.fromString(roomId);

        requireMembership(roomUuid, userId);

        List<RoomPinnedMessage> pins = roomPinnedMessageRepository.findByRoom_IdOrderByPinnedAtDesc(roomUuid);
        Map<String, Message> messagesById = new HashMap<>();
        List<String> ids = pins.stream().map(RoomPinnedMessage::getMessageId).toList();
        for (Message m : messageRepository.findAllById(ids)) {
            messagesById.put(m.getId(), m);
        }

        return pins.stream()
                .map(p -> messagesById.get(p.getMessageId()))
                .filter(java.util.Objects::nonNull)
                .map(this::toResponse)
                .toList();
    }

    @Override
    public List<ChatMessageResponse> getHistory(String roomId, UUID userId, Instant before, String beforeId, int size) {
        UUID roomUuid = UUID.fromString(roomId);

        requireMembership(roomUuid, userId);

        Pageable pageable = PageRequest.of(0, size, Sort.by(Sort.Direction.DESC, "createdAt", "id"));
        String uid = userId.toString();

        List<Message> messages = (before == null || beforeId == null)
                ? messageRepository.findVisibleHistory(roomId, uid, pageable)
                : messageRepository.findOlderThanCursor(roomId, uid, before, beforeId, pageable);

        return messages.stream().map(this::toResponse).toList();
    }


    @Override
    public Map<UUID, Long> getUnreadCountByRoom(UUID userId) {
        List<Roommember> myRooms = roomMemberRepository.findByUser_Id(userId);

        Map<UUID, Long> result = new HashMap<>();
        for (Roommember membership : myRooms) {
            UUID roomId = membership.getRoom().getId();

            Instant lastRead = membership.getLastReadAt() != null
                    ? membership.getLastReadAt()
                    : Instant.EPOCH;

            long unread = messageRepository.countByRoomIdAndCreatedAtAfterAndDeletedAtIsNull(
                    roomId.toString(), lastRead);

            result.put(roomId, unread);
        }
        return result;
    }

    private ChatMessageResponse toResponse(Message m) {
        boolean recalled = m.getDeletedAt() != null;
        return ChatMessageResponse.builder()
                .id(m.getId())
                .roomId(m.getRoomId())
                .senderId(m.getSenderId())
                .senderName(m.getSenderName())
                .senderAvatar(m.getSenderAvatar())
                .type(m.getType())
                .content(recalled ? null : m.getContent())
                .mediaUrls(recalled ? List.of() : m.getMediaUrls())
                .durationSeconds(recalled ? null : m.getDurationSeconds())
                .replyTo(m.getReplyTo())
                .forwardedFrom(m.getForwardedFrom())
                .recalled(recalled)
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
