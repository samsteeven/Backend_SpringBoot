package com.app.easypharma_backend.infrastructure.security;

import com.app.easypharma_backend.config.TestMailConfiguration;
import com.app.easypharma_backend.domain.auth.entity.User;
import com.app.easypharma_backend.domain.auth.entity.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ActiveProfiles;

import java.util.Collections;
import java.util.Random;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(TestMailConfiguration.class)
@ActiveProfiles("test")
class JwtServiceTest {

    @Autowired
    private JwtService jwtService;

    @Test
    void shouldGenerateAndValidateToken() {
        // Générer un email et un téléphone uniques
        String uniqueEmail = "jwt-test-" + UUID.randomUUID() + "@test.com";
        String uniquePhone = "+237" + String.format("%09d", new Random().nextInt(1000000000));

        User user = User.builder()
                .email(uniqueEmail)
                .password("password")
                .firstName("JWT")
                .lastName("Test")
                .phone(uniquePhone)
                .role(UserRole.PATIENT)
                .isActive(true)
                .isVerified(false)
                .build();

        // Générer un token
        String token = jwtService.generateToken(user);

        // Vérifier que le token est généré
        assertThat(token).isNotNull().isNotEmpty();

        // Créer UserDetails à partir de l'utilisateur
        UserDetails userDetails = createUserDetails(user);

        // Vérifier que le token est valide
        boolean isValid = jwtService.validateToken(token, userDetails);
        assertThat(isValid).isTrue();

        // Vérifier que l'email est extrait correctement
        String extractedEmail = jwtService.extractEmail(token);
        assertThat(extractedEmail).isEqualTo(uniqueEmail);
    }

    @Test
    void shouldExtractEmailFromToken() {
        // Générer un email et un téléphone uniques
        String uniqueEmail = "extract-email-" + UUID.randomUUID() + "@test.com";
        String uniquePhone = "+237" + String.format("%09d", new Random().nextInt(1000000000));

        User user = User.builder()
                .email(uniqueEmail)
                .password("password")
                .firstName("Extract")
                .lastName("Email")
                .phone(uniquePhone)
                .role(UserRole.PATIENT)
                .isActive(true)
                .isVerified(false)
                .build();

        String token = jwtService.generateToken(user);
        String extractedEmail = jwtService.extractEmail(token);

        assertThat(extractedEmail).isEqualTo(uniqueEmail);
    }

    @Test
    void shouldValidateTokenWithCorrectUser() {
        // Générer un email et un téléphone uniques
        String uniqueEmail = "validate-correct-" + UUID.randomUUID() + "@test.com";
        String uniquePhone = "+237" + String.format("%09d", new Random().nextInt(1000000000));

        User user = User.builder()
                .email(uniqueEmail)
                .password("password")
                .firstName("Validate")
                .lastName("Correct")
                .phone(uniquePhone)
                .role(UserRole.PATIENT)
                .isActive(true)
                .isVerified(false)
                .build();

        String token = jwtService.generateToken(user);

        // Créer UserDetails à partir de l'utilisateur
        UserDetails userDetails = createUserDetails(user);

        // Vérifier avec le bon utilisateur
        boolean isValid = jwtService.validateToken(token, userDetails);
        assertThat(isValid).isTrue();
    }

    @Test
    void shouldNotValidateTokenWithDifferentUser() {
        // Générer des emails et téléphones uniques
        String uniqueEmail1 = "validate-user1-" + UUID.randomUUID() + "@test.com";
        String uniquePhone1 = "+237" + String.format("%09d", new Random().nextInt(1000000000));

        String uniqueEmail2 = "validate-user2-" + UUID.randomUUID() + "@test.com";
        String uniquePhone2 = "+237" + String.format("%09d", new Random().nextInt(1000000000));

        User user1 = User.builder()
                .email(uniqueEmail1)
                .password("password")
                .firstName("Validate")
                .lastName("User1")
                .phone(uniquePhone1)
                .role(UserRole.PATIENT)
                .isActive(true)
                .isVerified(false)
                .build();

        User user2 = User.builder()
                .email(uniqueEmail2)
                .password("password")
                .firstName("Validate")
                .lastName("User2")
                .phone(uniquePhone2)
                .role(UserRole.PATIENT)
                .isActive(true)
                .isVerified(false)
                .build();

        String token = jwtService.generateToken(user1);

        // Créer UserDetails pour user2
        UserDetails userDetails2 = createUserDetails(user2);

        // Vérifier avec un utilisateur différent
        boolean isValid = jwtService.validateToken(token, userDetails2);
        assertThat(isValid).isFalse();
    }

    /**
     * Méthode utilitaire pour créer un UserDetails à partir d'un User
     */
    private UserDetails createUserDetails(User user) {
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())))
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(!user.getIsActive())
                .build();
    }
}