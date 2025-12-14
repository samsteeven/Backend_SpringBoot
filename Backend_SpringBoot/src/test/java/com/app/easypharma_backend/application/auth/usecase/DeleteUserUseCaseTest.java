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

import static org.mockito.Mockito.*;

class DeleteUserUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private DeleteUserUseCase deleteUserUseCase;

    private User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        user = User.builder()
                .email("test@example.com")
                .password("encodedPassword")
                .firstName("John")
                .lastName("Doe")
                .phone("+237600000000")
                .role(UserRole.PATIENT)
                .build();
    }

    @Test
    void shouldDeleteUserSuccessfully() {
        // Given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        // When
        deleteUserUseCase.execute("test@example.com");

        // Then
        verify(userRepository).findByEmail("test@example.com");
        verify(refreshTokenRepository).deleteByUser(user);
        verify(userRepository).delete(user);
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        // Given
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // When & Then
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> 
                deleteUserUseCase.execute("nonexistent@example.com"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Utilisateur non trouvé");

        verify(userRepository).findByEmail("nonexistent@example.com");
        verify(refreshTokenRepository, never()).deleteByUser(any(User.class));
        verify(userRepository, never()).delete(any(User.class));
    }
}