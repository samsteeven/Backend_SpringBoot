package com.app.easypharma_backend.application.auth.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class LogoutUseCase {

    private final RefreshTokenService refreshTokenService;
    // TODO: Ajouter TokenBlacklistService pour blacklister les JWT

    /**
     * Déconnecte un utilisateur en supprimant son refresh token
     */
    @Transactional
    public void execute(String refreshToken) {
        log.info("Déconnexion en cours");

        // Supprimer le refresh token
        refreshTokenService.deleteRefreshToken(refreshToken);

        // TODO: Ajouter l'access token à la blacklist
        // tokenBlacklistService.addToBlacklist(accessToken);

        log.info("Déconnexion réussie");
    }
}