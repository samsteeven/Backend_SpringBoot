package com.app.easypharma_backend.application.auth.usecase;

import com.app.easypharma_backend.domain.auth.entity.User;
import com.app.easypharma_backend.domain.auth.entity.UserRole;
import com.app.easypharma_backend.domain.auth.repository.UserRepository;
import com.app.easypharma_backend.domain.auth.repository.RefreshTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;

class DeleteUserByIdUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private DeleteUserByIdUseCase deleteUserByIdUseCase;

    private User user;
    private UUID userId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        userId = UUID.randomUUID();
        user = User.builder()
                .id(userId)
                .email("test@example.com")
                .password("encodedPassword")
                .firstName("John")
                .lastName("Doe")
                .phone("+237600000000")
                .role(UserRole.CUSTOMER)
                .build();
    }

    @Test
    void shouldDeleteUserByIdSuccessfully() {
        // Given
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // When
        deleteUserByIdUseCase.execute(userId);

        // Then
        verify(userRepository).findById(userId);
        verify(refreshTokenRepository).deleteByUser(user);
        verify(userRepository).delete(user);
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        // Given
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> 
                deleteUserByIdUseCase.execute(userId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Utilisateur non trouvé");

        verify(userRepository).findById(userId);
        verify(refreshTokenRepository, never()).deleteByUser(any(User.class));
        verify(userRepository, never()).delete(any(User.class));
    }
}