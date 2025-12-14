package com.app.easypharma_backend.application.auth.usecase;

import com.app.easypharma_backend.domain.auth.service.PasswordResetService;
import com.app.easypharma_backend.presentation.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResetPasswordUseCase {

    private final PasswordResetService passwordResetService;
    private final PasswordEncoder passwordEncoder;

    /**
     * Réinitialise le mot de passe de l'utilisateur
     */
    public void execute(String token, String newPassword) {
        log.info("Tentative de réinitialisation de mot de passe avec token");

        try {
            // Valider le token et obtenir l'utilisateur
            var user = passwordResetService.validateResetToken(token);

            // Hasher le nouveau mot de passe
            String encodedPassword = passwordEncoder.encode(newPassword);

            // Réinitialiser le mot de passe
            passwordResetService.resetPassword(token, encodedPassword);

            log.info("Mot de passe réinitialisé avec succès pour l'utilisateur: {}", user.getEmail());
        } catch (NotFoundException e) {
            log.warn("Tentative de réinitialisation avec un token invalide");
            throw e;
        } catch (Exception e) {
            log.error("Erreur lors de la réinitialisation du mot de passe", e);
            throw new RuntimeException("Erreur lors de la réinitialisation du mot de passe");
        }
    }
}