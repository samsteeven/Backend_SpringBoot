package com.app.easypharma_backend.application.auth.usecase;

import com.app.easypharma_backend.application.auth.dto.response.UserResponse;
import com.app.easypharma_backend.application.auth.mapper.UserMapper;
import com.app.easypharma_backend.domain.auth.entity.User;
import com.app.easypharma_backend.domain.auth.entity.UserRole;
import com.app.easypharma_backend.domain.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChangeUserRoleUseCase {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    /**
     * Change le rôle d'un utilisateur
     */
    @Transactional
    public UserResponse execute(UUID userId, UserRole newRole) {
        log.info("Changement de rôle pour l'utilisateur ID: {} vers le rôle: {}", userId, newRole);

        // Vérifier si l'utilisateur existe
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        // Mettre à jour le rôle
        user.setRole(newRole);

        // Sauvegarder les modifications
        User updatedUser = userRepository.save(user);
        log.info("Rôle utilisateur mis à jour avec succès: {} (ID: {})", updatedUser.getEmail(), updatedUser.getId());

        return userMapper.toResponse(updatedUser);
    }
}