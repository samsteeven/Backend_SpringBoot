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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UpdateUserProfileUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private UserUpdateHelper userUpdateHelper;

    @InjectMocks
    private UpdateUserProfileUseCase updateUserProfileUseCase;

    private User user;
    private UpdateUserRequest updateUserRequest;

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

        updateUserRequest = UpdateUserRequest.builder()
                .email("updated@example.com")
                .firstName("Updated")
                .lastName("Name")
                .phone("+237600000001")
                .build();
    }

    @Test
    void shouldUpdateUserProfileSuccessfully() {
        // Given
        UserResponse expectedUserResponse = new UserResponse();
        
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        doNothing().when(userUpdateHelper).validateAndUpdateUser(user, updateUserRequest);
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toResponse(user)).thenReturn(expectedUserResponse);

        // When
        var result = updateUserProfileUseCase.execute("test@example.com", updateUserRequest);

        // Then
        assertThat(result).isNotNull();
        verify(userRepository).findByEmail("test@example.com");
        verify(userUpdateHelper).validateAndUpdateUser(user, updateUserRequest);
        verify(userMapper).updateEntityFromRequest(updateUserRequest, user);
        verify(userRepository).save(user);
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        // Given
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> updateUserProfileUseCase.execute("nonexistent@example.com", updateUserRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Utilisateur non trouvé");

        verify(userRepository).findByEmail("nonexistent@example.com");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldThrowExceptionWhenEmailAlreadyExists() {
        // Given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        doThrow(new DuplicateResourceException("Cet email est déjà utilisé")).when(userUpdateHelper).validateAndUpdateUser(user, updateUserRequest);

        // When & Then
        assertThatThrownBy(() -> updateUserProfileUseCase.execute("test@example.com", updateUserRequest))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("Cet email est déjà utilisé");

        verify(userRepository).findByEmail("test@example.com");
        verify(userUpdateHelper).validateAndUpdateUser(user, updateUserRequest);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldThrowExceptionWhenPhoneAlreadyExists() {
        // Given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        doThrow(new DuplicateResourceException("Ce numéro de téléphone est déjà utilisé")).when(userUpdateHelper).validateAndUpdateUser(user, updateUserRequest);

        // When & Then
        assertThatThrownBy(() -> updateUserProfileUseCase.execute("test@example.com", updateUserRequest))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("Ce numéro de téléphone est déjà utilisé");

        verify(userRepository).findByEmail("test@example.com");
        verify(userUpdateHelper).validateAndUpdateUser(user, updateUserRequest);
        verify(userRepository, never()).save(any(User.class));
    }
}