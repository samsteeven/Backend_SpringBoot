package com.app.easypharma_backend.application.auth;

import com.app.easypharma_backend.application.auth.dto.request.LoginRequest;
import com.app.easypharma_backend.application.auth.dto.request.RefreshTokenRequest;
import com.app.easypharma_backend.application.auth.dto.request.RegisterRequest;
import com.app.easypharma_backend.application.auth.dto.response.AuthResponse;
import com.app.easypharma_backend.application.common.dto.ApiResponse;
import com.app.easypharma_backend.domain.auth.entity.UserRole;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.util.Random;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class FullAuthFlowIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;
    
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldCompleteFullAuthFlow() throws Exception {
        // Générer un email et un téléphone uniques
        String uniqueEmail = "full-flow-" + UUID.randomUUID() + "@test.com";
        String uniquePhone = String.format("%09d", new Random().nextInt(1000000000));

        // 1. Inscription
        RegisterRequest registerRequest = RegisterRequest.builder()
                .email(uniqueEmail)
                .password("Password123!")
                .firstName("Full")
                .lastName("Flow")
                .phone(uniquePhone)
                .role(UserRole.PATIENT)
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<RegisterRequest> registerEntity = new HttpEntity<>(registerRequest, headers);
        ResponseEntity<String> registerResponse = restTemplate.postForEntity("/api/v1/auth/register", registerEntity, String.class);

        assertThat(registerResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        // 2. Connexion
        LoginRequest loginRequest = LoginRequest.builder()
                .email(uniqueEmail)
                .password("Password123!")
                .build();

        HttpEntity<LoginRequest> loginEntity = new HttpEntity<>(loginRequest, headers);
        ResponseEntity<String> loginResponseEntity = restTemplate.postForEntity("/api/v1/auth/login", loginEntity, String.class);

        assertThat(loginResponseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(loginResponseEntity.getBody()).isNotNull();
        
        // Convertir la réponse JSON en ApiResponse<AuthResponse>
        JavaType loginResponseType = objectMapper.getTypeFactory().constructParametricType(ApiResponse.class, AuthResponse.class);
        ApiResponse<AuthResponse> loginApiResponse = objectMapper.readValue(loginResponseEntity.getBody(), loginResponseType);
        AuthResponse authResponse = loginApiResponse.getData();
        
        assertThat(authResponse).isNotNull();
        assertThat(authResponse.getAccessToken()).isNotNull().isNotEmpty();
        assertThat(authResponse.getRefreshToken()).isNotNull().isNotEmpty();

        // 3. Rafraîchissement du token
        String refreshToken = authResponse.getRefreshToken();
        RefreshTokenRequest refreshRequest = RefreshTokenRequest.builder()
                .refreshToken(refreshToken)
                .build();

        HttpEntity<RefreshTokenRequest> refreshEntity = new HttpEntity<>(refreshRequest, headers);
        ResponseEntity<String> refreshResponseEntity = restTemplate.postForEntity("/api/v1/auth/refresh-token", refreshEntity, String.class);

        assertThat(refreshResponseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(refreshResponseEntity.getBody()).isNotNull();
        
        // Convertir la réponse JSON en ApiResponse<AuthResponse>
        JavaType refreshResponseType = objectMapper.getTypeFactory().constructParametricType(ApiResponse.class, AuthResponse.class);
        ApiResponse<AuthResponse> refreshApiResponse = objectMapper.readValue(refreshResponseEntity.getBody(), refreshResponseType);
        AuthResponse refreshedResponse = refreshApiResponse.getData();
        
        assertThat(refreshedResponse).isNotNull();
        assertThat(refreshedResponse.getAccessToken()).isNotNull().isNotEmpty();
        assertThat(refreshedResponse.getRefreshToken()).isNotNull().isNotEmpty();
        // Le nouveau refresh token doit être différent de l'ancien
        assertThat(refreshedResponse.getRefreshToken()).isNotEqualTo(refreshToken);

        // 4. Déconnexion
        HttpEntity<RefreshTokenRequest> logoutEntity = new HttpEntity<>(refreshRequest, headers);
        ResponseEntity<String> logoutResponse = restTemplate.postForEntity("/api/v1/auth/logout", logoutEntity, String.class);

        assertThat(logoutResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}