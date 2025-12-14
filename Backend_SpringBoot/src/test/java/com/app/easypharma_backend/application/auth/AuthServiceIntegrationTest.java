package com.app.easypharma_backend.application.auth;

import com.app.easypharma_backend.application.auth.dto.request.LoginRequest;
import com.app.easypharma_backend.application.auth.dto.request.RegisterRequest;
import com.app.easypharma_backend.application.auth.dto.response.AuthResponse;
import com.app.easypharma_backend.application.auth.usecase.LoginUseCase;
import com.app.easypharma_backend.application.auth.usecase.RegisterUseCase;
import com.app.easypharma_backend.domain.auth.entity.User;
import com.app.easypharma_backend.domain.auth.entity.UserRole;
import com.app.easypharma_backend.domain.auth.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AuthServiceIntegrationTest {

    @Autowired
    private RegisterUseCase registerUseCase;

    @Autowired
    private LoginUseCase loginUseCase;

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldRegisterUserSuccessfully() {
        // Générer un email et un téléphone uniques
        String uniqueEmail = "auth-service-" + UUID.randomUUID() + "@test.com";
        String uniquePhone = String.format("%09d", new Random().nextInt(1000000000));

        RegisterRequest request = RegisterRequest.builder()
                .email(uniqueEmail)
                .password("Password123!")
                .firstName("AuthService")
                .lastName("Test")
                .phone(uniquePhone)
                .role(UserRole.PATIENT)
                .build();

        // Exécuter l'inscription
        AuthResponse response = registerUseCase.execute(request);

        // Vérifier la réponse
        assertThat(response).isNotNull();
        assertThat(response.getUser()).isNotNull();
        assertThat(response.getUser().getEmail()).isEqualTo(uniqueEmail);
        assertThat(response.getUser().getFirstName()).isEqualTo("AuthService");
        assertThat(response.getUser().getLastName()).isEqualTo("Test");

        // Vérifier que l'utilisateur est bien enregistré en base
        User savedUser = userRepository.findByEmail(uniqueEmail).orElseThrow();
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getEmail()).isEqualTo(uniqueEmail);
        assertThat(savedUser.getFirstName()).isEqualTo("AuthService");
        assertThat(savedUser.getLastName()).isEqualTo("Test");
        assertThat(savedUser.getPhone()).isEqualTo(uniquePhone);
    }

    @Test
    void shouldLoginUserSuccessfully() {
        // Générer un email et un téléphone uniques
        String uniqueEmail = "auth-login-" + UUID.randomUUID() + "@test.com";
        String uniquePhone = "+237" + String.format("%09d", new Random().nextInt(1000000000));

        // Créer un utilisateur d'abord
        RegisterRequest registerRequest = RegisterRequest.builder()
                .email(uniqueEmail)
                .password("Password123!")
                .firstName("Login")
                .lastName("Test")
                .phone(uniquePhone)
                .role(UserRole.PATIENT)
                .build();

        registerUseCase.execute(registerRequest);

        // Tester la connexion
        LoginRequest loginRequest = LoginRequest.builder()
                .email(uniqueEmail)
                .password("Password123!")
                .build();

        AuthResponse response = loginUseCase.execute(loginRequest);

        // Vérifier la réponse
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isNotNull().isNotEmpty();
        assertThat(response.getRefreshToken()).isNotNull().isNotEmpty();
        assertThat(response.getUser().getEmail()).isEqualTo(uniqueEmail);
    }

    @Test
    void shouldThrowExceptionForInvalidCredentials() {
        // Générer un email et un téléphone uniques
        String uniqueEmail = "invalid-creds-" + UUID.randomUUID() + "@test.com";
        String uniquePhone = "+237" + String.format("%09d", new Random().nextInt(1000000000));

        // Créer un utilisateur
        RegisterRequest registerRequest = RegisterRequest.builder()
                .email(uniqueEmail)
                .password("Password123!")
                .firstName("Invalid")
                .lastName("Creds")
                .phone(uniquePhone)
                .role(UserRole.PATIENT)
                .build();

        registerUseCase.execute(registerRequest);

        // Essayer de se connecter avec un mauvais mot de passe
        LoginRequest loginRequest = LoginRequest.builder()
                .email(uniqueEmail)
                .password("WrongPassword!")
                .build();

        // Vérifier que l'exception est levée
        assertThatThrownBy(() -> loginUseCase.execute(loginRequest))
                .isInstanceOf(RuntimeException.class); // Remplacer par l'exception spécifique si nécessaire
    }
}