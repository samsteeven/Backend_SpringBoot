package com.app.easypharma_backend.application.auth.usecase;

import com.app.easypharma_backend.application.auth.dto.request.LoginRequest;
import com.app.easypharma_backend.application.auth.dto.response.AuthResponse;
import com.app.easypharma_backend.application.auth.dto.response.UserResponse;
import com.app.easypharma_backend.application.auth.mapper.UserMapper;
import com.app.easypharma_backend.domain.auth.entity.User;
import com.app.easypharma_backend.domain.auth.repository.UserRepository;
import com.app.easypharma_backend.infrastructure.security.JwtService;
import com.app.easypharma_backend.presentation.exception.AccountDisabledException;
import com.app.easypharma_backend.presentation.exception.AuthenticationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoginUseCase {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    /**
     * Authentifie un utilisateur et génère les tokens JWT
     *
     * @param request Les credentials (email + password)
     * @return Les tokens d'authentification et les infos utilisateur
     * @throws AuthenticationException si les credentials sont invalides
     * @throws AccountDisabledException si le compte est désactivé
     */
    @Transactional
    public AuthResponse execute(LoginRequest request) {
        log.info("Tentative de connexion pour l'email: {}", request.getEmail());

        // 1. Récupérer l'utilisateur par email
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    log.warn("Tentative de connexion avec un email inexistant: {}", request.getEmail());
                    return new AuthenticationException("Email ou mot de passe incorrect");
                });

        // 2. Vérifier le mot de passe
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Tentative de connexion avec un mot de passe incorrect pour: {}", request.getEmail());
            throw new AuthenticationException("Email ou mot de passe incorrect");
        }

        // 3. Vérifier si le compte est actif
        if (!user.getIsActive()) {
            log.warn("Tentative de connexion avec un compte désactivé: {}", request.getEmail());
            throw new AccountDisabledException("Votre compte a été désactivé. Contactez le support.");
        }

        // 4. Générer le token JWT
        String accessToken = jwtService.generateToken(user);
        log.debug("Token JWT généré pour l'utilisateur: {}", user.getEmail());

        // 5. Générer le refresh token
        String refreshToken = refreshTokenService.createRefreshToken(user);
        log.debug("Refresh token généré pour l'utilisateur: {}", user.getEmail());

        // 6. Mapper l'utilisateur en DTO
        UserResponse userResponse = userMapper.toResponse(user);

        // 7. Construire la réponse
        log.info("Connexion réussie pour l'utilisateur: {} (ID: {})", user.getEmail(), user.getId());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpirationTime())
                .user(userResponse)
                .build();
    }
}