package com.app.easypharma_backend.application.auth.usecase;

import com.app.easypharma_backend.domain.auth.entity.User;
import com.app.easypharma_backend.domain.auth.repository.UserRepository;
import com.app.easypharma_backend.domain.auth.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeleteUserUseCase {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    /**
     * Supprime le compte utilisateur
     */
    @Transactional
    public void execute(String userEmail) {
        log.info("Suppression du compte utilisateur: {}", userEmail);

        // Vérifier si l'utilisateur existe
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        // Supprimer les refresh tokens associés
        refreshTokenRepository.deleteByUser(user);

        // Supprimer l'utilisateur
        userRepository.delete(user);
        log.info("Compte utilisateur supprimé avec succès: {} (ID: {})", user.getEmail(), user.getId());
    }
}