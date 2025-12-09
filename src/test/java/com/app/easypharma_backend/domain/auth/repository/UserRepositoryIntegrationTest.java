package com.app.easypharma_backend.domain.auth.repository;

import com.app.easypharma_backend.domain.auth.entity.User;
import com.app.easypharma_backend.domain.auth.entity.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;
import java.util.Random;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldSaveAndFindUser() {
        // Générer un email et un téléphone uniques
        String uniqueEmail = "repo-user-" + UUID.randomUUID() + "@test.com";
        String uniquePhone = String.format("%09d", new Random().nextInt(1000000000));

        User user = User.builder()
                .email(uniqueEmail)
                .password("encodedPassword")
                .firstName("John")
                .lastName("Doe")
                .phone(uniquePhone)
                .role(UserRole.PATIENT)
                .isActive(true)
                .isVerified(false)
                .build();

        // Sauvegarder l'utilisateur
        User savedUser = userRepository.save(user);

        // Vérifier que l'utilisateur est sauvegardé
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getEmail()).isEqualTo(uniqueEmail);
        assertThat(savedUser.getPhone()).isEqualTo(uniquePhone);

        // Trouver l'utilisateur par ID
        Optional<User> foundById = userRepository.findById(savedUser.getId());
        assertThat(foundById).isPresent();
        assertThat(foundById.get().getEmail()).isEqualTo(uniqueEmail);

        // Trouver l'utilisateur par email
        Optional<User> foundByEmail = userRepository.findByEmail(uniqueEmail);
        assertThat(foundByEmail).isPresent();
        assertThat(foundByEmail.get().getPhone()).isEqualTo(uniquePhone);

        // Trouver l'utilisateur par téléphone
        Optional<User> foundByPhone = userRepository.findByPhone(uniquePhone);
        assertThat(foundByPhone).isPresent();
        assertThat(foundByPhone.get().getEmail()).isEqualTo(uniqueEmail);
    }

    @Test
    void shouldCheckIfUserExists() {
        // Générer un email et un téléphone uniques
        String uniqueEmail = "exists-check-" + UUID.randomUUID() + "@test.com";
        String uniquePhone = "+237" + String.format("%09d", new Random().nextInt(1000000000));

        User user = User.builder()
                .email(uniqueEmail)
                .password("encodedPassword")
                .firstName("Jane")
                .lastName("Doe")
                .phone(uniquePhone)
                .role(UserRole.PATIENT)
                .isActive(true)
                .isVerified(false)
                .build();

        // Sauvegarder l'utilisateur
        userRepository.save(user);

        // Vérifier si l'utilisateur existe
        boolean existsByEmail = userRepository.existsByEmail(uniqueEmail);
        assertThat(existsByEmail).isTrue();

        boolean existsByPhone = userRepository.existsByPhone(uniquePhone);
        assertThat(existsByPhone).isTrue();

        // Vérifier qu'un utilisateur inexistant n'existe pas
        boolean notExists = userRepository.existsByEmail("nonexistent@test.com");
        assertThat(notExists).isFalse();
        
        boolean notExistsPhone = userRepository.existsByPhone("+237999999999");
        assertThat(notExistsPhone).isFalse();
    }

    @Test
    void shouldUpdateUser() {
        // Générer un email et un téléphone uniques
        String uniqueEmail= "update-user-" + UUID.randomUUID() + "@test.com";
        String uniquePhone = "+237" + String.format("%09d", new Random().nextInt(1000000000));

        User user = User.builder()
                .email(uniqueEmail)
                .password("encodedPassword")
                .firstName("Original")
                .lastName("Name")
                .phone(uniquePhone)
                .role(UserRole.PATIENT)
                .isActive(true)
                .isVerified(false)
                .build();

        // Sauvegarder l'utilisateur
        User savedUser = userRepository.save(user);

        // Mettre à jour l'utilisateur
        savedUser.setFirstName("Updated");
        savedUser.setLastName("Name");
        User updatedUser = userRepository.save(savedUser);

        // Vérifier la mise à jour
        assertThat(updatedUser.getFirstName()).isEqualTo("Updated");
        assertThat(updatedUser.getLastName()).isEqualTo("Name");
        assertThat(updatedUser.getEmail()).isEqualTo(uniqueEmail);
    }

    @Test
    void shouldDeleteUser() {
        // Générer un email et un téléphone uniques
        String uniqueEmail = "delete-user-" + UUID.randomUUID() + "@test.com";
        String uniquePhone = "+237" + String.format("%09d", new Random().nextInt(1000000000));

        User user = User.builder()
                .email(uniqueEmail)
                .password("encodedPassword")
                .firstName("ToDelete")
                .lastName("User")
                .phone(uniquePhone)
                .role(UserRole.PATIENT)
                .isActive(true)
                .isVerified(false)
                .build();

        // Sauvegarder l'utilisateur
        User savedUser = userRepository.save(user);

        // Vérifier qu'il existe
        assertThat(userRepository.findById(savedUser.getId())).isPresent();

        // Supprimer l'utilisateur
        userRepository.deleteById(savedUser.getId());

        // Vérifier qu'il n'existe plus
        assertThat(userRepository.findById(savedUser.getId())).isEmpty();
    }

    @Test
    void shouldFindByEmailIgnoreCase() {
        // Générer un email et un téléphone uniques
        String uniqueEmail = "case-sensitive-" + UUID.randomUUID() + "@test.com";
        String uniquePhone = "+237" + String.format("%09d", new Random().nextInt(1000000000));

        User user = User.builder()
                .email(uniqueEmail.toUpperCase()) // Email en majuscules
                .password("encodedPassword")
                .firstName("Case")
                .lastName("Sensitive")
                .phone(uniquePhone)
                .role(UserRole.PATIENT)
                .isActive(true)
                .isVerified(false)
                .build();

        // Sauvegarder l'utilisateur
        User savedUser = userRepository.save(user);

        // Trouver l'utilisateur avec un email en minuscules
        Optional<User> found = userRepository.findByEmail(uniqueEmail.toLowerCase());
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(savedUser.getId());
    }
}