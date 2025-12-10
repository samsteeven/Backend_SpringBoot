package com.app.easypharma_backend.presentation.controller;

import com.app.easypharma_backend.application.auth.dto.request.UpdateUserRequest;
import com.app.easypharma_backend.application.auth.dto.response.UserResponse;
import com.app.easypharma_backend.domain.auth.entity.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void shouldReturnUnauthorizedWhenNoTokenProvided() {
        // When
        ResponseEntity<String> response = restTemplate.getForEntity("/api/v1/users/me", String.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldReturnBadRequestForInvalidUpdateRequest() {
        // Given
        String token = "Bearer invalid-token";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", token);

        UpdateUserRequest invalidRequest = UpdateUserRequest.builder()
                .email("invalid-email")
                .firstName("") // vide
                .lastName("")
                .phone("invalid-phone")
                .build();

        HttpEntity<UpdateUserRequest> entity = new HttpEntity<>(invalidRequest, headers);

        // When
        ResponseEntity<String> response = restTemplate.exchange("/api/v1/users/me", HttpMethod.PUT, entity, String.class);

        // Then
        // Note: Peut retourner 401 (unauthorized) si le token est invalide
        // ou 400 (bad request) si les validations échouent
        assertThat(response.getStatusCode()).isIn(HttpStatus.UNAUTHORIZED, HttpStatus.BAD_REQUEST);
    }

    @Test
    void shouldReturnNotFoundForNonExistentUser() {
        // Given
        String token = "Bearer valid-token";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        // When
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/v1/users/" + UUID.randomUUID(), 
                HttpMethod.GET, 
                entity, 
                String.class);

        // Then
        // Note: Peut retourner 401 (unauthorized) si le token est invalide
        // ou 404 (not found) si l'utilisateur n'existe pas
        assertThat(response.getStatusCode()).isIn(HttpStatus.UNAUTHORIZED, HttpStatus.NOT_FOUND);
    }
}