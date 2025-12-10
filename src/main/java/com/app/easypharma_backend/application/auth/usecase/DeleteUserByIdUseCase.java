package com.app.easypharma_backend.application.auth.usecase;

import com.app.easypharma_backend.domain.auth.entity.User;
import com.app.easypharma_backend.domain.auth.repository.UserRepository;
import com.app.easypharma_backend.domain.auth.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeleteUserByIdUseCase {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    /**
     * Supprime un utilisateur par son ID (réservé aux administrateurs)
     */
    @Transactional
    public void execute(UUID userId) {
        log.info("Suppression de l'utilisateur ID: {}", userId);

        // Vérifier si l'utilisateur existe
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        // Supprimer les refresh tokens associés
        refreshTokenRepository.deleteByUser(user);

        // Supprimer l'utilisateur
        userRepository.delete(user);
        log.info("Utilisateur supprimé avec succès: {} (ID: {})", user.getEmail(), user.getId());
    }
}