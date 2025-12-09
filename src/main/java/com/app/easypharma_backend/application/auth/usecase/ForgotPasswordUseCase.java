package com.app.easypharma_backend.application.auth.usecase;

import com.app.easypharma_backend.domain.auth.service.PasswordResetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ForgotPasswordUseCase {

    private final PasswordResetService passwordResetService;
    // TODO: Injecter NotificationService pour envoyer l'email

    /**
     * Initie le processus de réinitialisation de mot de passe
     */
    public void execute(String email) {
        log.info("Demande de réinitialisation de mot de passe pour l'email: {}", email);

        try {
            // Générer le token de réinitialisation
            String token = passwordResetService.generateResetToken(email);

            // TODO: Envoyer l'email avec le lien de réinitialisation
            // String resetLink = "http://frontend-url/reset-password?token=" + token;
            // notificationService.sendPasswordResetEmail(email, resetLink);

            log.info("Token de réinitialisation généré et envoyé pour: {}", email);
        } catch (Exception e) {
            // Ne pas révéler si l'email existe ou non pour des raisons de sécurité
            log.warn("Tentative de réinitialisation de mot de passe pour un email non existant: {}", email);
        }
    }
}