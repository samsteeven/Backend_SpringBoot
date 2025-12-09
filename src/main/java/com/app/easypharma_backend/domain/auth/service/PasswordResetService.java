package com.app.easypharma_backend.domain.auth.service;

import com.app.easypharma_backend.domain.auth.entity.User;
import com.app.easypharma_backend.domain.auth.repository.UserRepository;
import com.app.easypharma_backend.presentation.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetService {

    private final UserRepository userRepository;
    
    // Stockage temporaire des tokens (en production, utiliser Redis ou DB)
    private final Map<String, PasswordResetToken> resetTokens = new HashMap<>();
    
    @Value("${app.jwt.expiration}")
    private Long tokenExpiration;

    /**
     * Génère un token de réinitialisation de mot de passe
     */
    public String generateResetToken(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Utilisateur non trouvé avec cet email"));

        // Supprimer l'ancien token s'il existe
        resetTokens.entrySet().removeIf(entry -> 
                entry.getValue().getUser().equals(user) || entry.getValue().isExpired());

        // Générer un nouveau token
        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken(token, user, 
                LocalDateTime.now().plusSeconds(tokenExpiration / 1000));
        
        resetTokens.put(token, resetToken);
        log.info("Token de réinitialisation généré pour l'utilisateur: {}", email);
        
        return token;
    }

    /**
     * Valide un token de réinitialisation
     */
    public User validateResetToken(String token) {
        PasswordResetToken resetToken = resetTokens.get(token);
        if (resetToken == null || resetToken.isExpired()) {
            throw new NotFoundException("Token de réinitialisation invalide ou expiré");
        }
        return resetToken.getUser();
    }

    /**
     * Réinitialise le mot de passe
     */
    public void resetPassword(String token, String newPassword) {
        User user = validateResetToken(token);
        
        user.setPassword(newPassword); // Le hashage se fera dans le use case
        userRepository.save(user);
        
        // Supprimer le token utilisé
        resetTokens.remove(token);
        log.info("Mot de passe réinitialisé pour l'utilisateur: {}", user.getEmail());
    }

    /**
     * Nettoie les tokens expirés
     */
    public void cleanupExpiredTokens() {
        resetTokens.entrySet().removeIf(entry -> entry.getValue().isExpired());
        log.info("Tokens de réinitialisation expirés nettoyés");
    }

    /**
     * Classe interne pour représenter un token de réinitialisation
     */
    @lombok.Getter
    @lombok.AllArgsConstructor
    private static class PasswordResetToken {
        private final String token;
        private final User user;
        private final LocalDateTime expiryDate;

        public boolean isExpired() {
            return LocalDateTime.now().isAfter(expiryDate);
        }
    }
}