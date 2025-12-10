package com.app.easypharma_backend.application.auth.usecase;

import com.app.easypharma_backend.domain.auth.entity.RefreshToken;
import com.app.easypharma_backend.domain.auth.entity.User;
import com.app.easypharma_backend.domain.auth.entity.UserRole;
import com.app.easypharma_backend.domain.auth.repository.RefreshTokenRepository;
import com.app.easypharma_backend.presentation.exception.TokenExpiredException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    private RefreshTokenService refreshTokenService;

    private User user;

    @BeforeEach
    void setUp() {
        try (AutoCloseable mocks = MockitoAnnotations.openMocks(this)) {
            // Définir explicitement la valeur refreshTokenDurationMs pour les tests
            refreshTokenService = new RefreshTokenService(refreshTokenRepository);
            org.springframework.test.util.ReflectionTestUtils.setField(refreshTokenService, "refreshTokenDurationMs", 604800000L);

            // Générer un téléphone unique
            String uniquePhone = "+237" + String.format("%09d", new Random().nextInt(1000000000));

            user = User.builder()
                    .id(UUID.randomUUID())
                    .email("test@refresh.com")
                    .password("password")
                    .firstName("Refresh")
                    .lastName("Test")
                    .phone(uniquePhone)
                    .role(UserRole.CUSTOMER)
                    .isActive(true)
                    .isVerified(false)
                    .build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void shouldCreateRefreshToken() {
        // Given
        when(refreshTokenRepository.findByUser(user)).thenReturn(Optional.empty());
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArguments()[0]);

        // When
        String token = refreshTokenService.createRefreshToken(user);

        // Then
        assertThat(token).isNotNull().isNotEmpty();
        verify(refreshTokenRepository).findByUser(user);
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void shouldDeleteOldTokenWhenCreatingNewOne() {
        // Given
        RefreshToken oldToken = RefreshToken.builder()
                .id(UUID.randomUUID())
                .token("old-token")
                .user(user)
                .expiresAt(LocalDateTime.now().plusDays(1))
                .build();

        when(refreshTokenRepository.findByUser(user)).thenReturn(Optional.of(oldToken));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArguments()[0]);

        // When
        String token = refreshTokenService.createRefreshToken(user);

        // Then
        assertThat(token).isNotNull().isNotEmpty();
        verify(refreshTokenRepository).findByUser(user);
        verify(refreshTokenRepository).delete(oldToken);
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void shouldValidateRefreshToken() {
        // Given
        String tokenString = UUID.randomUUID().toString();
        RefreshToken token = RefreshToken.builder()
                .id(UUID.randomUUID())
                .token(tokenString)
                .user(user)
                .expiresAt(LocalDateTime.now().plusDays(1))
                .build();

        when(refreshTokenRepository.findByToken(tokenString)).thenReturn(Optional.of(token));

        // When
        User result = refreshTokenService.validateRefreshToken(tokenString);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("test@refresh.com");
        verify(refreshTokenRepository).findByToken(tokenString);
    }

    @Test
    void shouldThrowExceptionForInvalidToken() {
        // Given
        when(refreshTokenRepository.findByToken("invalid-token")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> refreshTokenService.validateRefreshToken("invalid-token"))
                .isInstanceOf(TokenExpiredException.class)
                .hasMessage("Refresh token invalide");

        verify(refreshTokenRepository).findByToken("invalid-token");
    }

    @Test
    void shouldThrowExceptionForExpiredToken() {
        // Given
        String tokenString = UUID.randomUUID().toString();
        RefreshToken expiredToken = RefreshToken.builder()
                .id(UUID.randomUUID())
                .token(tokenString)
                .user(user)
                .expiresAt(LocalDateTime.now().minusDays(1)) // Expired
                .build();

        when(refreshTokenRepository.findByToken(tokenString)).thenReturn(Optional.of(expiredToken));

        // When & Then
        assertThatThrownBy(() -> refreshTokenService.validateRefreshToken(tokenString))
                .isInstanceOf(TokenExpiredException.class)
                .hasMessage("Refresh token expiré");

        verify(refreshTokenRepository).findByToken(tokenString);
        verify(refreshTokenRepository).delete(expiredToken);
    }

    @Test
    void shouldDeleteRefreshToken() {
        // Given
        String tokenString = UUID.randomUUID().toString();
        RefreshToken token = RefreshToken.builder()
                .id(UUID.randomUUID())
                .token(tokenString)
                .user(user)
                .expiresAt(LocalDateTime.now().plusDays(1))
                .build();

        when(refreshTokenRepository.findByToken(tokenString)).thenReturn(Optional.of(token));

        // When
        refreshTokenService.deleteRefreshToken(tokenString);

        // Then
        verify(refreshTokenRepository).findByToken(tokenString);
        verify(refreshTokenRepository).delete(token);
    }
}