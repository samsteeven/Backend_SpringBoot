package com.app.easypharma_backend.application.auth.usecase;

import com.app.easypharma_backend.application.auth.dto.request.RefreshTokenRequest;
import com.app.easypharma_backend.application.auth.dto.response.AuthResponse;
import com.app.easypharma_backend.application.auth.dto.response.UserResponse;
import com.app.easypharma_backend.application.auth.mapper.UserMapper;
import com.app.easypharma_backend.domain.auth.entity.User;
import com.app.easypharma_backend.infrastructure.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenUseCase {

    private final RefreshTokenService refreshTokenService;
    private final JwtService jwtService;
    private final UserMapper userMapper;

    /**
     * Rafraîchit l'access token en utilisant le refresh token
     */
    @Transactional
    public AuthResponse execute(RefreshTokenRequest request) {
        log.info("Tentative de rafraîchissement du token");

        // 1. Valider le refresh token et récupérer l'utilisateur
        User user = refreshTokenService.validateRefreshToken(request.getRefreshToken());

        // 2. Générer un nouvel access token
        String newAccessToken = jwtService.generateToken(user);

        // 3. Générer un nouveau refresh token (rotation)
        String newRefreshToken = refreshTokenService.createRefreshToken(user);

        // 4. Mapper l'utilisateur
        UserResponse userResponse = userMapper.toResponse(user);

        log.info("Token rafraîchi avec succès pour l'utilisateur: {}", user.getEmail());

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpirationTime())
                .user(userResponse)
                .build();
    }
}