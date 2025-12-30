package com.app.easypharma_backend.domain.notification.service.interfaces;

import com.app.easypharma_backend.domain.auth.entity.User;
import com.app.easypharma_backend.domain.notification.entity.NotificationType;

import java.util.UUID;

public interface NotificationService {
    void sendInAppNotification(User user, String title, String message, NotificationType type);

    void sendSms(String phone, String message);

    void sendPushNotification(User user, String title, String message);

    void sendEmail(String email, String subject, String body);
}
