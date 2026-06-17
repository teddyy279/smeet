package com.karina.smeet.modules.notification.consumer;


import com.karina.smeet.common.exception.AppException;
import com.karina.smeet.modules.notification.config.EmailRabbitMQConfig;
import com.karina.smeet.modules.notification.dto.OtpEmailMessage;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Component
@RequiredArgsConstructor
@Slf4j

public class EmailConsumer {
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @RabbitListener(queues = EmailRabbitMQConfig.EMAIL_OTP_QUEUE)
    public void consumeOtpEmail(OtpEmailMessage message) {
        log.info("Received a message from RabbitMQ, starting to prepare sending OTP to: {}", message.toEmail());

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(fromEmail, "S-Meet Team");
            helper.setTo(message.toEmail());
            helper.setSubject("[S-Meet] - Mã xác thực OTP của bạn");
            String htmlContent = """
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #eee; border-radius: 10px;">
                    <h2 style="color: #4A90E2; text-align: center;">Xác Thực Tài Khoản S-Meet</h2>
                    <p>Chào bạn,</p>
                    <p>Bạn vừa yêu cầu mã OTP để thao tác trên hệ thống S-Meet. Mã xác thực của bạn là:</p>
                    <div style="text-align: center; margin: 30px 0;">
                        <span style="font-size: 32px; font-weight: bold; letter-spacing: 5px; color: #E14CCA; background-color: #f5f5f5; padding: 10px 20px; border-radius: 5px; border: 1px dashed #ccc;">
                            %s
                        </span>
                    </div>
                    <p style="color: #ff4d4f; font-size: 13px;">* Lưu ý: Mã OTP này chỉ có hiệu lực trong vòng 2 phút. Tuyệt đối không chia sẻ mã này cho bất kỳ ai.</p>
                    <hr style="border: none; border-top: 1px solid #eee; margin-top: 30px;">
                    <p style="font-size: 11px; color: #999; text-align: center;">Đây là email tự động từ hệ thống S-Meet, vui lòng không phản hồi email này.</p>
                </div>
            """.formatted(message.otp());
            helper.setText(htmlContent, true);
            mailSender.send(mimeMessage);
            log.info("The OTP email has been successfully sent to: {}", message.toEmail());
        } catch (Exception exception) {
            log.error("A serious error occurred while sending the OTP email to: {}", message.toEmail(), exception);
            throw new RuntimeException("OTP email delivery failed: " + exception.getMessage(), exception);
        }
    }
}
