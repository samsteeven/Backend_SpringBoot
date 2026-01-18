package com.app.easypharma_backend.application.auth;

import com.app.easypharma_backend.application.auth.dto.request.LoginRequest;
import com.app.easypharma_backend.application.auth.dto.request.RefreshTokenRequest;
import com.app.easypharma_backend.application.auth.dto.request.RegisterRequest;
import com.app.easypharma_backend.application.auth.dto.response.AuthResponse;
import com.app.easypharma_backend.application.common.dto.ApiResponse;
import com.app.easypharma_backend.config.TestMailConfiguration;
import com.app.easypharma_backend.domain.auth.entity.UserRole;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.http.*;

import java.util.Random;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(TestMailConfiguration.class)
class AuthIntegrationTest {

        @Autowired
        private TestRestTemplate restTemplate;

        @Autowired
        private ObjectMapper objectMapper;

        @Test
        void shouldRegisterLoginAndRefreshTokenSuccessfully() throws Exception {
                // Générer un email unique pour éviter les conflits
                String uniqueEmail = "integration-" + UUID.randomUUID() + "@test.com";

                // Générer un numéro de téléphone unique avec le bon format (+237 + 9 chiffres)
                Random random = new Random();
                String uniquePhone = String.format("%09d", random.nextInt(1000000000));

                // 1. Test d'inscription
                RegisterRequest registerRequest = RegisterRequest.builder()
                                .email(uniqueEmail)
                                .password("Password123!")
                                .firstName("Test")
                                .lastName("User")
                                .phone(uniquePhone)
                                .role(UserRole.PATIENT)
                                .build();

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);

                HttpEntity<RegisterRequest> registerEntity = new HttpEntity<>(registerRequest, headers);
                ResponseEntity<String> registerResponse = restTemplate.postForEntity("/api/v1/auth/register",
                                registerEntity, String.class);

                // Afficher le corps de la réponse pour le débogage
                System.out.println("Status d'inscription: " + registerResponse.getStatusCode());
                System.out.println("Corps de la réponse d'inscription: " + registerResponse.getBody());

                // Vérifier que l'inscription réussit
                assertThat(registerResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(registerResponse.getBody()).contains("Inscription réussie");

                // 2. Test de connexion
                LoginRequest loginRequest = LoginRequest.builder()
                                .email(uniqueEmail)
                                .password("Password123!")
                                .build();

                HttpEntity<LoginRequest> loginEntity = new HttpEntity<>(loginRequest, headers);
                ResponseEntity<String> loginResponseEntity = restTemplate.postForEntity("/api/v1/auth/login",
                                loginEntity, String.class);

                // Afficher le corps de la réponse pour le débogage
                System.out.println("Status de connexion: " + loginResponseEntity.getStatusCode());
                System.out.println("Corps de la réponse de connexion: " + loginResponseEntity.getBody());

                // Vérifier que la connexion réussit
                assertThat(loginResponseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(loginResponseEntity.getBody()).isNotNull();

                // Convertir la réponse JSON en ApiResponse<AuthResponse>
                String loginResponseBody = loginResponseEntity.getBody();
                JavaType loginResponseType = objectMapper.getTypeFactory().constructParametricType(ApiResponse.class,
                                AuthResponse.class);
                ApiResponse<AuthResponse> loginApiResponse = objectMapper.readValue(loginResponseBody,
                                loginResponseType);
                AuthResponse loginResponse = loginApiResponse.getData();

                assertThat(loginResponse).isNotNull();
                assertThat(loginResponse.getAccessToken()).isNotNull().isNotEmpty();
                assertThat(loginResponse.getRefreshToken()).isNotNull().isNotEmpty();

                String refreshToken = loginResponse.getRefreshToken();

                // 3. Test de rafraîchissement du token
                RefreshTokenRequest refreshRequest = RefreshTokenRequest.builder()
                                .refreshToken(refreshToken)
                                .build();

                HttpEntity<RefreshTokenRequest> refreshEntity = new HttpEntity<>(refreshRequest, headers);
                ResponseEntity<String> refreshResponseEntity = restTemplate.postForEntity("/api/v1/auth/refresh-token",
                                refreshEntity, String.class);

                // Afficher le corps de la réponse pour le débogage
                System.out.println("Status de rafraîchissement: " + refreshResponseEntity.getStatusCode());
                System.out.println("Corps de la réponse de rafraîchissement: " + refreshResponseEntity.getBody());

                // Convertir la réponse JSON en ApiResponse<AuthResponse>
                String refreshResponseBody = refreshResponseEntity.getBody();
                JavaType refreshResponseType = objectMapper.getTypeFactory().constructParametricType(ApiResponse.class,
                                AuthResponse.class);
                ApiResponse<AuthResponse> refreshApiResponse = objectMapper.readValue(refreshResponseBody,
                                refreshResponseType);
                AuthResponse refreshResponse = refreshApiResponse.getData();

                assertThat(refreshResponseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(refreshResponse).isNotNull();
                assertThat(refreshResponse.getAccessToken()).isNotNull().isNotEmpty();
                assertThat(refreshResponse.getRefreshToken()).isNotNull().isNotEmpty();

                // 4. Test de déconnexion
                HttpEntity<RefreshTokenRequest> logoutEntity = new HttpEntity<>(refreshRequest, headers);
                ResponseEntity<String> logoutResponse = restTemplate.postForEntity("/api/v1/auth/logout", logoutEntity,
                                String.class);

                // Afficher le corps de la réponse pour le débogage
                System.out.println("Status de déconnexion: " + logoutResponse.getStatusCode());
                System.out.println("Corps de la réponse de déconnexion: " + logoutResponse.getBody());

                assertThat(logoutResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(logoutResponse.getBody()).contains("Déconnexion réussie");

        }
}