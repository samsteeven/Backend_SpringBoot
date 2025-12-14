package com.app.easypharma_backend.application.auth.usecase;

import com.app.easypharma_backend.application.auth.dto.response.UserResponse;
import com.app.easypharma_backend.application.auth.mapper.UserMapper;
import com.app.easypharma_backend.domain.auth.entity.User;
import com.app.easypharma_backend.domain.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class GetUserProfileUseCase {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    /**
     * Récupère le profil de l'utilisateur courant
     */
    @Transactional(readOnly = true)
    public UserResponse execute(String email) {
        log.info("Récupération du profil utilisateur pour: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        return userMapper.toResponse(user);
    }
}