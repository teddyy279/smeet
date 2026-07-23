package com.karina.smeet.modules.chat.dto.request;

import com.karina.smeet.common.exception.AppException;
import com.karina.smeet.common.exception.ErrorCode;
import com.karina.smeet.entity.mongo.Message;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record SendMessageRequest(
        @NotBlank(message = "ROOM_ID_BLANK")
        String roomId,

        @NotNull(message = "MESSAGE_TYPE_NULL")
        Message.Type type,

        String content,

        List<String> mediaUrls,

        // chỉ dùng khi type = AUDIO
        Integer durationSeconds,

        String replyTo
) {
        public SendMessageRequest {
                if (type == Message.Type.TEXT && (content == null || content.isBlank()))
                        throw new AppException(ErrorCode.MESSAGE_CONTENT_REQUIRED);

                if ((type == Message.Type.IMAGE || type == Message.Type.FILE)
                        && (mediaUrls == null || mediaUrls.isEmpty()))
                        throw new AppException(ErrorCode.MEDIA_URL_REQUIRED);

                if (type == Message.Type.AUDIO) {
                        if (mediaUrls == null || mediaUrls.isEmpty())
                                throw new AppException(ErrorCode.MEDIA_URL_REQUIRED);
                        if (durationSeconds == null || durationSeconds <= 0)
                                throw new AppException(ErrorCode.AUDIO_DURATION_REQUIRED);
                }
        }
}