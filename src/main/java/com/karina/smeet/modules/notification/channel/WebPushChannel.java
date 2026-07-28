package com.karina.smeet.modules.notification.channel;

import com.karina.smeet.modules.notification.dto.NotificationMessage;
import com.karina.smeet.modules.user.service.OnlineStatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j

public class WebPushChannel implements NotificationChannel{
    private final OnlineStatusService onlineStatusService;

    @Override
    public boolean supports(NotificationMessage message) {
        return !onlineStatusService.isOnline(message.getToUserId());
    }

    @Override
    public void send(NotificationMessage message) {
        // TODO: implement Web Push API thật sự sau
        // hiện tại chỉ log — notification vẫn được lưu DB nên
        // user vào lại app vẫn thấy được, chỉ thiếu popup ngoài trình duyệt
        log.info("[WebPush - chưa implement] toUser={}, title={}",
                message.getToUserId(), message.getTitle());
    }
}
