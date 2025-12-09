package com.app.easypharma_backend.domain.auth.service;

import com.app.easypharma_backend.domain.auth.entity.User;
import com.app.easypharma_backend.domain.auth.entity.RefreshToken;
import com.app.easypharma_backend.domain.auth.repository.UserRepository;
import com.app.easypharma_backend.domain.auth.repository.RefreshTokenRepository;
import com.app.easypharma_backend.infrastructure.security.JwtService;
import com.app.easypharma_backend.presentation.exception.TokenExpiredException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserDomainService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;

    /**
     * Génère un access token JWT pour un utilisateur
     */
    public String generateAccessToken(User user) {
        return jwtService.generateToken(user);
    }

    /**
     * Génère un refresh token pour un utilisateur
     */
    @Transactional
    public String generateRefreshToken(User user) {
        // Supprimer l'ancien refresh token s'il existe
        refreshTokenRepository.findByUser(user).ifPresent(refreshTokenRepository::delete);

        // Créer un nouveau refresh token
        RefreshToken refreshToken = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .user(user)
                .expiresAt(LocalDateTime.now().plusDays(7)) // 7 jours par défaut
                .build();

        refreshTokenRepository.save(refreshToken);
        return refreshToken.getToken();
    }

    /**
     * Valide un refresh token et retourne l'utilisateur associé
     */
    @Transactional(readOnly = true)
    public User validateRefreshTokenAndGetUser(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new TokenExpiredException("Refresh token invalide"));

        if (refreshToken.isExpired()) {
            refreshTokenRepository.delete(refreshToken);
            throw new TokenExpiredException("Refresh token expiré");
        }

        return refreshToken.getUser();
    }

    /**
     * Invalide un refresh token
     */
    @Transactional
    public void invalidateRefreshToken(String token) {
        refreshTokenRepository.findByToken(token).ifPresent(refreshTokenRepository::delete);
    }

    /**
     * Nettoie les refresh tokens expirés
     */
    @Transactional
    public void cleanupExpiredTokens() {
        refreshTokenRepository.deleteExpiredTokens(LocalDateTime.now());
    }
}