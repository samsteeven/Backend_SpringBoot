package com.app.easypharma_backend.infrastructure.firebase;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
public class FirebaseService {

    /**
     * Envoie une notification push à un token spécifique
     * 
     * @param token FCM Token du destinataire
     * @param title Titre de la notification
     * @param body  Corps de la notification
     * @param data  Données supplémentaires (facultatif)
     */
    public void sendPushNotification(String token, String title, String body, Map<String, String> data) {
        if (token == null || token.isEmpty()) {
            log.warn("FCM Token is missing. Cannot send notification.");
            return;
        }

        try {
            Notification notification = Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build();

            Message.Builder messageBuilder = Message.builder()
                    .setToken(token)
                    .setNotification(notification);

            if (data != null && !data.isEmpty()) {
                messageBuilder.putAllData(data);
            }

            Message message = messageBuilder.build();

            // L'envoi ne fonctionne que si FirebaseApp est initialisé
            String response = FirebaseMessaging.getInstance().send(message);
            log.info("Successfully sent FCM message: {}", response);

        } catch (IllegalStateException e) {
            log.warn("[FCM SIMULATION] App not initialized. Notification would be: {} - {}", title, body);
        } catch (FirebaseMessagingException e) {
            log.error("FCM Error sending message to {}: {}", token, e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error sending FCM message: {}", e.getMessage());
        }
    }
}
