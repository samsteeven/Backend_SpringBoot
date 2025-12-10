package com.app.easypharma_backend.application.auth.usecase;

import com.app.easypharma_backend.application.auth.dto.request.UpdateUserRequest;
import com.app.easypharma_backend.application.auth.dto.response.UserResponse;
import com.app.easypharma_backend.application.auth.mapper.UserMapper;
import com.app.easypharma_backend.application.auth.util.UserUpdateHelper;
import com.app.easypharma_backend.domain.auth.entity.User;
import com.app.easypharma_backend.domain.auth.entity.UserRole;
import com.app.easypharma_backend.domain.auth.repository.UserRepository;
import com.app.easypharma_backend.presentation.exception.DuplicateResourceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UpdateUserByIdUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock private UserUpdateHelper userUpdateHelper;

    @InjectMocks
    private UpdateUserByIdUseCase updateUserByIdUseCase;

    private User user;
    private UpdateUserRequest updateUserRequest;
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
                .role(UserRole.PATIENT)
                .build();

        updateUserRequest = UpdateUserRequest.builder()
                .email("updated@example.com")
                .firstName("Updated")
                .lastName("Name")
                .phone("+237600000001")
                .build();
    }

    @Test
    void shouldUpdateUserByIdSuccessfully() {
        // Given
        UserResponse expectedUserResponse = UserResponse.builder()
                .id(userId)
                .email("updated@example.com")
                .firstName("Updated")
                .lastName("Name")
                .phone("+237600000001")
                .role(UserRole.PATIENT)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("updated@example.com")).thenReturn(false);
        when(userRepository.existsByPhone("+237600000001")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toResponse(user)).thenReturn(expectedUserResponse);

        // When
        var result = updateUserByIdUseCase.execute(userId, updateUserRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("updated@example.com");
        verify(userRepository).findById(userId);
        verify(userUpdateHelper).validateAndUpdateUser(user, updateUserRequest);
        verify(userRepository).existsByEmail("updated@example.com");
        verify(userRepository).existsByPhone("+237600000001");
        verify(userMapper).updateEntityFromRequest(updateUserRequest, user);
        verify(userRepository).save(user);
        verify(userMapper).toResponse(user);
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        // Given
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> updateUserByIdUseCase.execute(userId, updateUserRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Utilisateur non trouvé");

        verify(userRepository).findById(userId);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldThrowExceptionWhenEmailAlreadyExists() {
        // Given
        UUID anotherUserId = UUID.randomUUID();
        User anotherUser = User.builder()
                .id(anotherUserId)
                .email("updated@example.com")
                .password("anotherPassword")
                .firstName("Another")
                .lastName("User")
                .phone("+237600000002")
                .role(UserRole.PATIENT)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("updated@example.com")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> updateUserByIdUseCase.execute(userId, updateUserRequest))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("Cet email est déjà utilisé");

        verify(userRepository).findById(userId);
        verify(userRepository).existsByEmail("updated@example.com");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldThrowExceptionWhenPhoneAlreadyExists() {
        // Given
        UUID anotherUserId = UUID.randomUUID();
        User anotherUser = User.builder()
                .id(anotherUserId)
                .email("another@example.com")
                .password("anotherPassword")
                .firstName("Another")
                .lastName("User")
                .phone("+237600000001")
                .role(UserRole.PATIENT)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("updated@example.com")).thenReturn(false);
        when(userRepository.existsByPhone("+237600000001")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> updateUserByIdUseCase.execute(userId, updateUserRequest))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("Ce numéro de téléphone est déjà utilisé");

        verify(userRepository).findById(userId);
        verify(userRepository).existsByEmail("updated@example.com");
        verify(userRepository).existsByPhone("+237600000001");
        verify(userRepository, never()).save(any(User.class));
    }
}