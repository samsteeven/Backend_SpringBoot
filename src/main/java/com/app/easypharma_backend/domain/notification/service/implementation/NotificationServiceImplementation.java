package com.app.easypharma_backend.domain.notification.service.implementation;

import com.app.easypharma_backend.domain.auth.entity.User;
import com.app.easypharma_backend.domain.notification.entity.Notification;
import com.app.easypharma_backend.domain.notification.entity.NotificationType;
import com.app.easypharma_backend.domain.notification.repository.NotificationRepository;
import com.app.easypharma_backend.domain.notification.service.interfaces.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImplementation implements NotificationService {

    private final NotificationRepository notificationRepository;

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

        // Simuler également un Push quand une notif in-app est créée
        sendPushNotification(user, title, message);
    }

    @Override
    public void sendSms(String phone, String message) {
        // Simulation d'envoi SMS (Integration Twilio/D7Networks plus tard)
        log.info("[SMS SIMULATION] To: {} | Message: {}", phone, message);
    }

    @Override
    public void sendPushNotification(User user, String title, String message) {
        // Simulation de Push Notification (Firebase FCM plus tard)
        log.info("[PUSH SIMULATION] To: {} | Title: {} | Message: {}", user.getEmail(), title, message);
    }

    @Override
    public void sendEmail(String email, String subject, String body) {
        // Simulation d'envoi d'Email (JavaMailSender plus tard)
        log.info("[EMAIL SIMULATION] To: {} | Subject: {} | Body: {}", email, subject, body);
    }
}
