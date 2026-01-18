package com.app.easypharma_backend.domain.notification.service.implementation;

import com.app.easypharma_backend.domain.auth.entity.User;
import com.app.easypharma_backend.domain.notification.entity.Notification;
import com.app.easypharma_backend.domain.notification.entity.NotificationType;
import com.app.easypharma_backend.domain.notification.repository.NotificationRepository;
import com.app.easypharma_backend.domain.notification.service.interfaces.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImplementation implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final JavaMailSender mailSender;
    private final com.app.easypharma_backend.infrastructure.firebase.FirebaseService firebaseService;

    @Override
    @Transactional
    public void sendInAppNotification(User user, String title, String message, NotificationType type) {
        Notification notification = Notification.builder()
                .user(user)
                .title(title)
                .message(message)
                .type(type)
                .isRead(false)
                .build();
        notificationRepository.save(notification);
        log.info("In-app notification saved for user {}: {}", user.getEmail(), title);

        // Simulation d'un Push quand une notif in-app est créée
        sendPushNotification(user, title, message);
    }

    @Override
    public void sendSms(String phone, String message) {
        // Simulation d'envoi SMS (Integration Twilio/D7Networks plus tard)
        log.info("[SMS SIMULATION] To: {} | Message: {}", phone, message);
    }

    @Override
    public void sendPushNotification(User user, String title, String message) {
        if (user.getFcmToken() != null && !user.getFcmToken().isEmpty()) {
            firebaseService.sendPushNotification(user.getFcmToken(), title, message, null);
        } else {
            log.info("[PUSH SIMULATION] No token for {}. Title: {} | Message: {}", user.getEmail(), title, message);
        }
    }

    @Override
    public void sendEmail(String email, String subject, String body) {
        try {
            // Supporter le HTML si le body contient du HTML, sinon envoyer en plain text
            if (body != null && (body.contains("<html") || body.contains("<a ") || body.contains("<br"))) {
                MimeMessage mimeMessage = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
                helper.setTo(email);
                helper.setSubject(subject);
                helper.setFrom("no-reply@easypharma.com");
                helper.setText(body, true); // true = isHtml
                mailSender.send(mimeMessage);
            } else {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(email);
                message.setSubject(subject);
                message.setText(body);
                message.setFrom("no-reply@easypharma.com");
                mailSender.send(message);
            }

            log.info("Email sent successfully to: {}", email);
        } catch (MessagingException me) {
            log.error("Failed to build/send MIME email to {}: {}", email, me.getMessage());
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", email, e.getMessage());
        }
    }
}
