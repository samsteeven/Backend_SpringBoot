package com.app.easypharma_backend.application.auth;

import com.app.easypharma_backend.application.auth.dto.request.RegisterRequest;
import com.app.easypharma_backend.application.auth.dto.request.UpdateUserRequest;
import com.app.easypharma_backend.application.auth.dto.response.AuthResponse;
import com.app.easypharma_backend.application.auth.dto.response.UserResponse;
import com.app.easypharma_backend.application.auth.usecase.RegisterUseCase;
import com.app.easypharma_backend.application.auth.usecase.UpdateUserProfileUseCase;
import com.app.easypharma_backend.domain.auth.entity.UserRole;
import com.app.easypharma_backend.domain.auth.repository.RefreshTokenRepository;
import com.app.easypharma_backend.domain.auth.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class UserManagementIntegrationTest {

    @Autowired
    private RegisterUseCase registerUseCase;

    @Autowired
    private UpdateUserProfileUseCase updateUserProfileUseCase;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @AfterEach
    void tearDown() {
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void shouldRegisterAndUpdateUserSuccessfully() {
        String uniqueEmail = "usermgmt-" + UUID.randomUUID() + "@test.com";
        String updatedEmail = "updated-" + UUID.randomUUID() + "@test.com";

        // 1. Inscription
        RegisterRequest registerRequest = RegisterRequest.builder()
                .email(uniqueEmail)
                .password("Password123!")
                .firstName("User")
                .lastName("Management")
                .phone("+2376" + String.format("%08d", new java.util.Random().nextInt(100000000)))
                .role(UserRole.PATIENT)
                .build();

        AuthResponse authResponse = registerUseCase.execute(registerRequest);
        UserResponse registeredUser = authResponse.getUser();
        assertThat(registeredUser).isNotNull();
        assertThat(registeredUser.getEmail()).isEqualTo(uniqueEmail);

        // 2. Mise à jour du profil
        UpdateUserRequest updateRequest = UpdateUserRequest.builder()
                .email(updatedEmail)
                .firstName("Updated")
                .lastName("User")
                .phone("+2376" + String.format("%08d", new java.util.Random().nextInt(100000000)))
                .address("123 Updated Street")
                .city("Updated City")
                .build();

        UserResponse updatedUser = updateUserProfileUseCase.execute(uniqueEmail, updateRequest);
        assertThat(updatedUser).isNotNull();
        assertThat(updatedUser.getEmail()).isEqualTo(updatedEmail);
        assertThat(updatedUser.getFirstName()).isEqualTo("Updated");
        assertThat(updatedUser.getLastName()).isEqualTo("User");
        assertThat(updatedUser.getAddress()).isEqualTo("123 Updated Street");
        assertThat(updatedUser.getCity()).isEqualTo("Updated City");

        // 3. Vérifier que l'utilisateur est bien mis à jour en base
        var userOpt = userRepository.findByEmail(updatedEmail);
        assertThat(userOpt).isPresent();
        assertThat(userOpt.get().getFirstName()).isEqualTo("Updated");
        assertThat(userOpt.get().getLastName()).isEqualTo("User");
    }
}