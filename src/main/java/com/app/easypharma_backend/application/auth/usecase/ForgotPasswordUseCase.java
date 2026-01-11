package com.app.easypharma_backend.application.auth.usecase;

import com.app.easypharma_backend.domain.auth.service.AuditService;
import com.app.easypharma_backend.domain.auth.service.PasswordResetService;
import com.app.easypharma_backend.domain.notification.service.interfaces.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ForgotPasswordUseCase {

    private final PasswordResetService passwordResetService;
    private final NotificationService notificationService;
    private final AuditService auditService;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    /**
     * Initie le processus de réinitialisation de mot de passe
     */
    public void execute(String email, String ipAddress, String userAgent) {
        log.info("Demande de réinitialisation de mot de passe pour l'email: {}", email);

        try {
            // Générer le token de réinitialisation
            String token = passwordResetService.generateResetToken(email);

            // Lien de réinitialisation dynamique
            String resetLink = frontendUrl + "/reset-password?token=" + token;

            String subject = "Réinitialisation de votre mot de passe - EasyPharma";
            String body = "Bonjour,\n\n" +
                    "Vous avez demandé la réinitialisation de votre mot de passe pour votre compte EasyPharma.\n" +
                    "Veuillez cliquer sur le lien ci-dessous pour définir un nouveau mot de passe :\n\n" +
                    resetLink + "\n\n" +
                    "Ce lien expirera bientôt.\n" +
                    "Si vous n'êtes pas à l'origine de cette demande, vous pouvez ignorer cet email.\n\n" +
                    "Cordialement,\n" +
                    "L'équipe EasyPharma";

            notificationService.sendEmail(email, subject, body);

            // Log d'audit pour succès
            auditService.logPasswordResetRequest(email, ipAddress, userAgent, true, "Email envoyé avec succès");

            log.info("Email de réinitialisation envoyé pour: {}", email);
        } catch (Exception e) {
            // Log d'audit pour échec
            auditService.logPasswordResetRequest(email, ipAddress, userAgent, false, e.getMessage());

            // Ne pas révéler si l'email existe ou non pour des raisons de sécurité
            log.warn("Tentative de réinitialisation de mot de passe pour un email non existant ou erreur: {} - {}",
                    email, e.getMessage());
        }
    }
}