package com.app.easypharma_backend.application.auth.util;

import com.app.easypharma_backend.application.auth.dto.request.UpdateUserRequest;
import com.app.easypharma_backend.domain.auth.entity.User;
import com.app.easypharma_backend.domain.auth.repository.UserRepository;
import com.app.easypharma_backend.presentation.exception.DuplicateResourceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserUpdateHelper {

    private final UserRepository userRepository;

    /**
     * Valide et met à jour les informations utilisateur
     */
    public void validateAndUpdateUser(User user, UpdateUserRequest request) {
        // Vérifier si l'email est déjà utilisé par un autre utilisateur
        if (!user.getEmail().equals(request.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new DuplicateResourceException("Cet email est déjà utilisé");
            }
            user.setEmail(request.getEmail());
        }

        // Vérifier si le téléphone est déjà utilisé par un autre utilisateur
        if (!user.getPhone().equals(request.getPhone())) {
            if (userRepository.existsByPhone(request.getPhone())) {
                throw new DuplicateResourceException("Ce numéro de téléphone est déjà utilisé");
            }
            user.setPhone(request.getPhone());
        }
    }
}