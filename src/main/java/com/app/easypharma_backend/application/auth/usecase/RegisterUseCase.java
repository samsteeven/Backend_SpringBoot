package com.app.easypharma_backend.application.auth.usecase;

import com.app.easypharma_backend.application.auth.dto.request.RegisterRequest;
import com.app.easypharma_backend.application.auth.dto.response.AuthResponse;
import com.app.easypharma_backend.application.auth.dto.response.UserResponse;
import com.app.easypharma_backend.application.auth.mapper.UserMapper;
import com.app.easypharma_backend.domain.auth.entity.User;
import com.app.easypharma_backend.domain.auth.repository.UserRepository;
import com.app.easypharma_backend.domain.auth.service.UserDomainService;
import com.app.easypharma_backend.presentation.exception.DuplicateResourceException;
import com.app.easypharma_backend.infrastructure.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RegisterUseCase {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final UserDomainService userDomainService;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    // TODO: Injecter NotificationService pour envoyer email de vérification

    /**
     * Inscrit un nouvel utilisateur et le connecte automatiquement
     *
     * @param request Les données d'inscription
     * @return AuthResponse contenant les tokens et l'utilisateur
     * @throws DuplicateResourceException si l'email ou le téléphone existe déjà
     */
    @Transactional
    public AuthResponse execute(RegisterRequest request) {
        log.info("Tentative d'inscription pour l'email: {}", request.getEmail());

        // 1. Vérifier si l'email existe déjà
        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Tentative d'inscription avec un email déjà utilisé: {}", request.getEmail());
            throw new DuplicateResourceException("Cet email est déjà utilisé");
        }

        // 2. Vérifier si le téléphone existe déjà
        if (userRepository.existsByPhone(request.getPhone())) {
            log.warn("Tentative d'inscription avec un téléphone déjà utilisé: {}", request.getPhone());
            throw new DuplicateResourceException("Ce numéro de téléphone est déjà utilisé");
        }

        // 3. Mapper le DTO vers l'entité
        User user = userMapper.toEntity(request);

        // 4. Hasher le mot de passe
        String hashedPassword = passwordEncoder.encode(request.getPassword());
        user.setPassword(hashedPassword);

        // 5. Sauvegarder l'utilisateur
        User savedUser = userRepository.save(user);
        log.info("Utilisateur inscrit avec succès: {} (ID: {})", savedUser.getEmail(), savedUser.getId());

        // 6. Générer les tokens pour connexion automatique
        String accessToken = jwtService.generateToken(savedUser);
        Long expiresIn = jwtService.getExpirationTime();
        String refreshToken = refreshTokenService.createRefreshToken(savedUser);

        // 7. Construire la réponse
        UserResponse userResponse = userMapper.toResponse(savedUser);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(expiresIn)
                .user(userResponse)
                .build();
    }
}