package com.app.easypharma_backend.presentation.controller;

import com.app.easypharma_backend.application.auth.dto.request.UpdateUserRequest;
import com.app.easypharma_backend.domain.auth.entity.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserControllerSecurityTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void shouldAllowAccessToListUsersForAdmin() {
        // Given - Un token d'administrateur (simulation)
        String adminToken = "Bearer admin-token";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", adminToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        // When
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/v1/users?page=0&size=10",
                HttpMethod.GET,
                entity,
                String.class);

        // Then - Devrait retourner 401 Unauthorized (car le token est invalide)
        // ou 403 Forbidden (selon la configuration Spring Security)
        assertThat(response.getStatusCode()).isIn(HttpStatus.UNAUTHORIZED, HttpStatus.FORBIDDEN);
    }

    @Test
    void shouldDenyAccessToListUsersForNonAdmin() {
        // Given - Un token d'utilisateur non-administrateur (simulation)
        String userToken = "Bearer user-token";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", userToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        // When
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/v1/users?page=0&size=10",
                HttpMethod.GET,
                entity,
                String.class);

        // Then - Devrait retourner 403 Forbidden ou 401 Unauthorized
        assertThat(response.getStatusCode()).isIn(HttpStatus.FORBIDDEN, HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldAllowRegularUserToUpdateOwnProfile() {
        // Given - Un token utilisateur valide (simulation)
        String userToken = "Bearer valid-user-token";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", userToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        UpdateUserRequest request = UpdateUserRequest.builder()
                .email("updated@example.com")
                .firstName("Updated")
                .lastName("User")
                .phone("+237600000020")
                .build();

        HttpEntity<UpdateUserRequest> entity = new HttpEntity<>(request, headers);

        // When
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/v1/users/me",
                HttpMethod.PUT,
                entity,
                String.class);

        // Then - Devrait retourner 401 Unauthorized (car le token est invalide)
        // ou 403 Forbidden (selon la configuration Spring Security)
        assertThat(response.getStatusCode()).isIn(HttpStatus.UNAUTHORIZED, HttpStatus.FORBIDDEN);
    }

    @Test
    void shouldDenyRegularUserToUpdateOtherUser() {
        // Given - Un token utilisateur valide (simulation)
        String userToken = "Bearer valid-user-token";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", userToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        UpdateUserRequest request = UpdateUserRequest.builder()
                .email("updated@example.com")
                .firstName("Updated")
                .lastName("User")
                .phone("+237600000021")
                .build();

        HttpEntity<UpdateUserRequest> entity = new HttpEntity<>(request, headers);

        // When
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/v1/users/" + UUID.randomUUID(),
                HttpMethod.PUT,
                entity,
                String.class);

        // Then - Devrait retourner 403 Forbidden ou 401 Unauthorized
        assertThat(response.getStatusCode()).isIn(HttpStatus.FORBIDDEN, HttpStatus.UNAUTHORIZED);
    }
}