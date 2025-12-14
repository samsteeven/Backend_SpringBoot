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

    @Mock 
    private UserUpdateHelper userUpdateHelper;

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
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toResponse(user)).thenReturn(expectedUserResponse);
        doNothing().when(userUpdateHelper).validateAndUpdateUser(user, updateUserRequest);
        // Simulation du mapping
        doAnswer(invocation -> {
            UpdateUserRequest req = invocation.getArgument(0);
            User usr = invocation.getArgument(1);
            usr.setEmail(req.getEmail());
            usr.setFirstName(req.getFirstName());
            usr.setLastName(req.getLastName());
            usr.setPhone(req.getPhone());
            return null;
        }).when(userMapper).updateEntityFromRequest(updateUserRequest, user);

        // When
        var result = updateUserByIdUseCase.execute(userId, updateUserRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("updated@example.com");
        verify(userRepository).findById(userId);
        verify(userUpdateHelper).validateAndUpdateUser(user, updateUserRequest);
        verify(userRepository).save(user);
        verify(userMapper).toResponse(user);
        verify(userMapper).updateEntityFromRequest(updateUserRequest, user);
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
        verify(userUpdateHelper, never()).validateAndUpdateUser(any(), any());
    }

    @Test
    void shouldThrowExceptionWhenEmailAlreadyExists() {
        // Given
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        doThrow(new DuplicateResourceException("Cet email est déjà utilisé"))
                .when(userUpdateHelper).validateAndUpdateUser(user, updateUserRequest);

        // When & Then
        assertThatThrownBy(() -> updateUserByIdUseCase.execute(userId, updateUserRequest))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("Cet email est déjà utilisé");

        verify(userRepository).findById(userId);
        verify(userUpdateHelper).validateAndUpdateUser(user, updateUserRequest);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldThrowExceptionWhenPhoneAlreadyExists() {
        // Given
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        doThrow(new DuplicateResourceException("Ce numéro de téléphone est déjà utilisé"))
                .when(userUpdateHelper).validateAndUpdateUser(user, updateUserRequest);

        // When & Then
        assertThatThrownBy(() -> updateUserByIdUseCase.execute(userId, updateUserRequest))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("Ce numéro de téléphone est déjà utilisé");

        verify(userRepository).findById(userId);
        verify(userUpdateHelper).validateAndUpdateUser(user, updateUserRequest);
        verify(userRepository, never()).save(any(User.class));
    }
}