package com.app.easypharma_backend.application.auth.usecase;

import com.app.easypharma_backend.application.auth.dto.request.UpdateUserRequest;
import com.app.easypharma_backend.application.auth.dto.response.UserResponse;
import com.app.easypharma_backend.application.auth.mapper.UserMapper;
import com.app.easypharma_backend.application.auth.util.UserUpdateHelper;
import com.app.easypharma_backend.domain.auth.entity.User;
import com.app.easypharma_backend.domain.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UpdateUserProfileUseCase {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final UserUpdateHelper userUpdateHelper;

    /**
     * Met à jour le profil de l'utilisateur
     */
    @Transactional
    public UserResponse execute(String currentUserEmail, UpdateUserRequest request) {
        log.info("Mise à jour du profil utilisateur pour: {}", currentUserEmail);

        // Vérifier si l'utilisateur existe
        User user = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        // Mettre à jour les autres champs
        userMapper.updateEntityFromRequest(request, user);

        // Valider et mettre à jour les informations
        userUpdateHelper.validateAndUpdateUser(user, request);

        // Sauvegarder les modifications
        User updatedUser = userRepository.save(user);
        log.info("Profil utilisateur mis à jour avec succès: {} (ID: {})", updatedUser.getEmail(), updatedUser.getId());

        return userMapper.toResponse(updatedUser);
    }
}