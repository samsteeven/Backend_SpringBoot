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
       refreshTokenRepository.deleteAll();userRepository.deleteAll();
    }

    @Test
    void shouldRegisterAndUpdateUserSuccessfully() {
        // 1. Inscription
        RegisterRequest registerRequest = RegisterRequest.builder()
                .email("usermgmt@test.com")
                .password("Password123!")
                .firstName("User")
                .lastName("Management")
                .phone("+237600000010")
                .role(UserRole.PATIENT)
                .build();

        AuthResponse authResponse = registerUseCase.execute(registerRequest);
        UserResponse registeredUser = authResponse.getUser();
        assertThat(registeredUser).isNotNull();
        assertThat(registeredUser.getEmail()).isEqualTo("usermgmt@test.com");

        // 2. Mise à jour du profil
        UpdateUserRequest updateRequest = UpdateUserRequest.builder()
                .email("updateduser@test.com")
                .firstName("Updated")
                .lastName("User")
                .phone("+237600000011")
                .address("123 Updated Street")
                .city("Updated City")
                .build();

        UserResponse updatedUser = updateUserProfileUseCase.execute("usermgmt@test.com", updateRequest);
        assertThat(updatedUser).isNotNull();
        assertThat(updatedUser.getEmail()).isEqualTo("updateduser@test.com");
        assertThat(updatedUser.getFirstName()).isEqualTo("Updated");
        assertThat(updatedUser.getLastName()).isEqualTo("User");
        assertThat(updatedUser.getPhone()).isEqualTo("+237600000011");
        assertThat(updatedUser.getAddress()).isEqualTo("123 Updated Street");
        assertThat(updatedUser.getCity()).isEqualTo("Updated City");

        // 3. Vérifier que l'utilisateur est bien mis à jour en base
        var userOpt = userRepository.findByEmail("updateduser@test.com");
        assertThat(userOpt).isPresent();
        assertThat(userOpt.get().getFirstName()).isEqualTo("Updated");
        assertThat(userOpt.get().getLastName()).isEqualTo("User");
   }
}