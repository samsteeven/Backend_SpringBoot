package com.app.easypharma_backend.application.auth.usecase;

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
    // AuditService removed - use new audit.AuditLogService if needed

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

                // Lien de réinitialisation dynamique (web)
                String resetLink = frontendUrl + "/reset-password?token=" + token;
                // Lien custom scheme pour ouvrir l'application mobile
                String appLink = "easypharma://reset-password?token=" + token;

                String subject = "Réinitialisation de votre mot de passe - EasyPharma";

                // Corps HTML avec lien vers l'app puis fallback web
                String body = "<html><body>" +
                    "<p>Bonjour,</p>" +
                    "<p>Vous avez demandé la réinitialisation de votre mot de passe pour votre compte EasyPharma.</p>" +
                    "<p><strong>Ouvrir dans l'application :</strong><br/>" +
                    "<a href=\"" + appLink + "\">Ouvrir dans l'application</a></p>" +
                    "<p><strong>Ou utiliser le lien web :</strong><br/>" +
                    "<a href=\"" + resetLink + "\">Réinitialiser votre mot de passe</a></p>" +
                    "<p>Si le premier lien n'ouvre pas l'application, utilisez le lien web ci-dessus.</p>" +
                    "<p>Ce lien expirera bientôt. Si vous n'êtes pas à l'origine de cette demande, ignorez cet email.</p>" +
                    "<p>Cordialement,<br/>L'équipe EasyPharma</p>" +
                    "</body></html>";

                notificationService.sendEmail(email, subject, body);

            // Log d'audit pour succès
            // auditService.logPasswordResetRequest(email, ipAddress, userAgent, true,
            // "Email envoyé avec succès");

            log.info("Email de réinitialisation envoyé pour: {}", email);
        } catch (Exception e) {
            // Log d'audit pour échec
            // auditService.logPasswordResetRequest(email, ipAddress, userAgent, false,
            // e.getMessage());

            // Ne pas révéler si l'email existe ou non pour des raisons de sécurité
            log.warn("Tentative de réinitialisation de mot de passe pour un email non existant ou erreur: {} - {}",
                    email, e.getMessage());
        }
    }
}