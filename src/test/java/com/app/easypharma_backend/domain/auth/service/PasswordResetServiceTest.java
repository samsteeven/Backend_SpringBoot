package com.app.easypharma_backend.domain.auth.service;

import com.app.easypharma_backend.config.TestMailConfiguration;
import com.app.easypharma_backend.domain.auth.entity.User;
import com.app.easypharma_backend.domain.auth.entity.UserRole;
import com.app.easypharma_backend.domain.auth.repository.UserRepository;
import com.app.easypharma_backend.presentation.exception.NotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Import(TestMailConfiguration.class)
class PasswordResetServiceTest {

    @Autowired
    private PasswordResetService passwordResetService;

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldGenerateResetToken() {
        // Générer un email et un téléphone uniques
        String uniqueEmail = "reset-token-" + UUID.randomUUID() + "@test.com";
        String uniquePhone = String.format("%09d", new Random().nextInt(1000000000));

        // Créer un utilisateur
        User user = User.builder()
                .email(uniqueEmail)
                .password("oldPassword")
                .firstName("Reset")
                .lastName("Test")
                .phone(uniquePhone)
                .role(UserRole.PATIENT)
                .isActive(true)
                .isVerified(false)
                .build();

        User savedUser = userRepository.save(user);

        // Générer un token de réinitialisation
        String token = passwordResetService.generateResetToken(savedUser.getEmail());

        // Vérifier que le token est généré
        assertThat(token).isNotNull().isNotEmpty();

        // Vérifier que le token est valide en le validant
        User validatedUser = passwordResetService.validateResetToken(token);
        assertThat(validatedUser).isNotNull();
        assertThat(validatedUser.getEmail()).isEqualTo(savedUser.getEmail());
    }

    @Test
    void shouldValidateToken() {
        // Générer un email et un téléphone uniques
        String uniqueEmail = "validate-token-" + UUID.randomUUID() + "@test.com";
        String uniquePhone = "+237" + String.format("%09d", new Random().nextInt(1000000000));

        // Créer un utilisateur
        User user = User.builder()
                .email(uniqueEmail)
                .password("oldPassword")
                .firstName("Validate")
                .lastName("Test")
                .phone(uniquePhone)
                .role(UserRole.PATIENT)
                .isActive(true)
                .isVerified(false)
                .build();

        User savedUser = userRepository.save(user);

        // Générer un token
        String token = passwordResetService.generateResetToken(savedUser.getEmail());

        // Vérifier qu'il est valide immédiatement
        User validatedUser = passwordResetService.validateResetToken(token);
        assertThat(validatedUser).isNotNull();
        assertThat(validatedUser.getEmail()).isEqualTo(savedUser.getEmail());

        // Vérifier qu'un token invalide lance une exception
        assertThatThrownBy(() -> passwordResetService.validateResetToken("invalid-token"))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Token de réinitialisation invalide ou expiré");
    }

    @Test
    void shouldResetPassword() {
        // Générer un email et un téléphone uniques
        String uniqueEmail = "reset-password-" + UUID.randomUUID() + "@test.com";
        String uniquePhone = "+237" + String.format("%09d", new Random().nextInt(1000000000));

        // Créer un utilisateur
        User user = User.builder()
                .email(uniqueEmail)
                .password("oldPassword")
                .firstName("Password")
                .lastName("Reset")
                .phone(uniquePhone)
                .role(UserRole.PATIENT)
                .isActive(true)
                .isVerified(false)
                .build();

        User savedUser = userRepository.save(user);

        // Générer un token
        String token = passwordResetService.generateResetToken(savedUser.getEmail());

        // Réinitialiser le mot de passe
        String newPassword = "NewPassword123!";
        passwordResetService.resetPassword(token, newPassword);

        // Vérifier que le token n'est plus valide (il a été supprimé)
        assertThatThrownBy(() -> passwordResetService.validateResetToken(token))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Token de réinitialisation invalide ou expiré");
    }
}