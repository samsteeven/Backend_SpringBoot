package com.app.easypharma_backend.application.auth.usecase;

import com.app.easypharma_backend.domain.auth.entity.RefreshToken;
import com.app.easypharma_backend.domain.auth.entity.User;
import com.app.easypharma_backend.domain.auth.repository.RefreshTokenRepository;
import com.app.easypharma_backend.presentation.exception.TokenExpiredException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${app.jwt.refresh-expiration}")
    private Long refreshTokenDurationMs;

    /**
     * Crée un nouveau refresh token pour un utilisateur
     */
    @Transactional
    public String createRefreshToken(User user) {
        // Supprimer l'ancien refresh token s'il existe
        refreshTokenRepository.findByUser(user).ifPresent(refreshTokenRepository::delete);

        // Créer un nouveau refresh token
        RefreshToken refreshToken = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .user(user)
                .expiresAt(LocalDateTime.now().plusSeconds(refreshTokenDurationMs / 1000))
                .build();

        refreshTokenRepository.save(refreshToken);
        log.debug("Refresh token créé pour l'utilisateur: {}", user.getEmail());

        return refreshToken.getToken();
    }

    /**
     * Valide un refresh token et retourne l'utilisateur associé
     */
    @Transactional(readOnly = true)
    public User validateRefreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new TokenExpiredException("Refresh token invalide"));

        if (refreshToken.isExpired()) {
            log.warn("Tentative d'utilisation d'un refresh token expiré");
            refreshTokenRepository.delete(refreshToken);
            throw new TokenExpiredException("Refresh token expiré");
        }

        return refreshToken.getUser();
    }

    /**
     * Supprime un refresh token
     */
    @Transactional
    public void deleteRefreshToken(String token) {
        refreshTokenRepository.findByToken(token).ifPresent(refreshTokenRepository::delete);
    }

    /**
     * Supprime tous les refresh tokens expirés (scheduled task)
     */
    @Transactional
    public void cleanupExpiredTokens() {
        refreshTokenRepository.deleteExpiredTokens(LocalDateTime.now());
        log.info("Tokens expirés nettoyés");
    }
}
