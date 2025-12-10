package com.app.easypharma_backend.application.auth.util;

import com.app.easypharma_backend.application.auth.dto.request.UpdateUserRequest;
import com.app.easypharma_backend.domain.auth.entity.User;
import com.app.easypharma_backend.domain.auth.entity.UserRole;
import com.app.easypharma_backend.domain.auth.repository.UserRepository;
import com.app.easypharma_backend.presentation.exception.DuplicateResourceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;
class UserUpdateHelperTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserUpdateHelper userUpdateHelper;

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
                .role(UserRole.CUSTOMER)
                .build();

        updateUserRequest = UpdateUserRequest.builder()
                .email("updated@example.com")
                .firstName("Updated")
                .lastName("Name")
                .phone("+237600000001")
                .build();
    }

    @Test
    void shouldUpdateUserFieldsSuccessfully() {
        // Given
        when(userRepository.existsByEmail("updated@example.com")).thenReturn(false);
        when(userRepository.existsByPhone("+237600000001")).thenReturn(false);

        // When
        userUpdateHelper.validateAndUpdateUser(user, updateUserRequest);

        // Then
        verify(userRepository).existsByEmail("updated@example.com");
        verify(userRepository).existsByPhone("+237600000001");
        assertThat(user.getEmail()).isEqualTo("updated@example.com");
        assertThat(user.getPhone()).isEqualTo("+237600000001");
    }

    @Test
    void shouldThrowExceptionWhenEmailAlreadyExists() {
        // Given
        when(userRepository.existsByEmail("updated@example.com")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> userUpdateHelper.validateAndUpdateUser(user, updateUserRequest))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("Cet email est déjà utilisé");

        verify(userRepository).existsByEmail("updated@example.com");
        verify(userRepository, never()).existsByPhone(anyString());
    }

    @Test
    void shouldThrowExceptionWhenPhoneAlreadyExists() {
        // Given
        when(userRepository.existsByEmail("updated@example.com")).thenReturn(false);
        when(userRepository.existsByPhone("+237600000001")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> userUpdateHelper.validateAndUpdateUser(user, updateUserRequest))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("Ce numéro de téléphone est déjà utilisé");

        verify(userRepository).existsByEmail("updated@example.com");
        verify(userRepository).existsByPhone("+237600000001");
    }

    @Test
    void shouldNotCheckEmailWhenSameAsCurrent() {
        // Given
        updateUserRequest.setEmail("test@example.com"); // Same as current email
        when(userRepository.existsByPhone("+237600000001")).thenReturn(false);

        // When
        userUpdateHelper.validateAndUpdateUser(user, updateUserRequest);

        // Then
        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository).existsByPhone("+237600000001");
    }

    @Test
    void shouldNotCheckPhoneWhenSameAsCurrent() {
        // Given
        updateUserRequest.setPhone("+237600000000"); // Same as current phone
        when(userRepository.existsByEmail("updated@example.com")).thenReturn(false);

        // When
        userUpdateHelper.validateAndUpdateUser(user, updateUserRequest);

        // Then
        verify(userRepository).existsByEmail("updated@example.com");
        verify(userRepository, never()).existsByPhone(anyString());
    }
}