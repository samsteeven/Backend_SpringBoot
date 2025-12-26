package com.app.easypharma_backend.presentation.exception;

import com.app.easypharma_backend.application.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class GlobalExceptionHandler {

        private final Environment environment;

        /**
         * Gère les erreurs de validation (@Valid)
         */
        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(
                        MethodArgumentNotValidException ex) {

                Map<String, String> errors = new HashMap<>();
                ex.getBindingResult().getAllErrors().forEach((error) -> {
                        String fieldName = ((FieldError) error).getField();
                        String errorMessage = error.getDefaultMessage();
                        errors.put(fieldName, errorMessage);
                });

                log.warn("Erreur de validation: {}", errors);

                ApiResponse<Map<String, String>> response = ApiResponse.<Map<String, String>>builder()
                                .success(false)
                                .message("Erreur de validation")
                                .data(errors)
                                .timestamp(System.currentTimeMillis())
                                .build();

                return ResponseEntity
                                .status(HttpStatus.BAD_REQUEST)
                                .body(response);
        }

        /**
         * Gère les ressources en double (email, téléphone, etc.)
         */
        @ExceptionHandler(DuplicateResourceException.class)
        public ResponseEntity<ApiResponse<Void>> handleDuplicateResourceException(
                        DuplicateResourceException ex) {

                log.warn("Ressource en double: {}", ex.getMessage());

                return ResponseEntity
                                .status(HttpStatus.CONFLICT)
                                .body(ApiResponse.error(ex.getMessage()));
        }

        /**
         * Gère les erreurs d'authentification
         */
        @ExceptionHandler(AuthenticationException.class)
        public ResponseEntity<ApiResponse<Void>> handleAuthenticationException(
                        AuthenticationException ex) {

                log.warn("Erreur d'authentification: {}", ex.getMessage());

                return ResponseEntity
                                .status(HttpStatus.UNAUTHORIZED)
                                .body(ApiResponse.error(ex.getMessage()));
        }

        /**
         * Gère les comptes désactivés
         */
        @ExceptionHandler(AccountDisabledException.class)
        public ResponseEntity<ApiResponse<Void>> handleAccountDisabledException(
                        AccountDisabledException ex) {

                log.warn("Compte désactivé: {}", ex.getMessage());

                return ResponseEntity
                                .status(HttpStatus.FORBIDDEN)
                                .body(ApiResponse.error(ex.getMessage()));
        }

        /**
         * Gère les tokens expirés
         */
        @ExceptionHandler(TokenExpiredException.class)
        public ResponseEntity<ApiResponse<Void>> handleTokenExpiredException(
                        TokenExpiredException ex) {

                log.warn("Token expiré: {}", ex.getMessage());

                return ResponseEntity
                                .status(HttpStatus.UNAUTHORIZED)
                                .body(ApiResponse.error(ex.getMessage()));
        }

        /**
         * Gère les erreurs de parsing JSON
         */
        @ExceptionHandler(HttpMessageNotReadableException.class)
        public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadableException(
                        HttpMessageNotReadableException ex) {

                String errorMessage = "Format JSON invalide";

                // Extract more specific error information if available
                if (ex.getCause() != null) {
                        String causeMessage = ex.getCause().getMessage();
                        if (causeMessage != null) {
                                // Extract the specific parsing error
                                if (causeMessage.contains("Unexpected character")) {
                                        errorMessage = "Format JSON invalide: " + causeMessage.split(":")[0];
                                } else if (causeMessage.contains("was expecting")) {
                                        errorMessage = "Format JSON invalide: " + causeMessage;
                                } else {
                                        errorMessage = "Format JSON invalide: " + causeMessage;
                                }
                        }
                }

                log.warn("Erreur de parsing JSON: {}", errorMessage);
                log.debug("Détails complets de l'erreur:", ex);

                return ResponseEntity
                                .status(HttpStatus.BAD_REQUEST)
                                .body(ApiResponse.error(errorMessage));
        }

        /**
         * Gère les ressources non trouvées
         */
        @ExceptionHandler(NotFoundException.class)
        public ResponseEntity<ApiResponse<Void>> handleNotFoundException(
                        NotFoundException ex) {

                log.warn("Ressource non trouvée: {}", ex.getMessage());

                return ResponseEntity
                                .status(HttpStatus.NOT_FOUND)
                                .body(ApiResponse.error(ex.getMessage()));
        }

        /**
         * Gère les parties manquantes dans une requête Multipart
         */
        @ExceptionHandler(org.springframework.web.multipart.support.MissingServletRequestPartException.class)
        public ResponseEntity<ApiResponse<Void>> handleMissingServletRequestPartException(
                        org.springframework.web.multipart.support.MissingServletRequestPartException ex) {

                log.warn("Partie manquante dans la requête: {}", ex.getMessage());

                return ResponseEntity
                                .status(HttpStatus.BAD_REQUEST)
                                .body(ApiResponse.error(
                                                "La partie requise '" + ex.getRequestPartName() + "' est manquante."));
        }

        /**
         * Gère toutes les autres exceptions
         */
        @ExceptionHandler(Exception.class)
        public ResponseEntity<ApiResponse<Void>> handleGeneralException(Exception ex) {
                log.error("Erreur interne du serveur: {}", ex.getMessage(), ex);

                String errorMessage;

                // Vérifier si on est en mode développement
                boolean isDevMode = environment.matchesProfiles("dev");

                if (isDevMode) {
                        // En développement, on expose les détails pour faciliter le debug
                        if (ex.getMessage() != null && !ex.getMessage().isEmpty()) {
                                errorMessage = ex.getClass().getSimpleName() + ": " + ex.getMessage();
                        } else {
                                errorMessage = "Erreur de type: " + ex.getClass().getSimpleName();
                        }
                } else {
                        // En production, message générique pour la sécurité
                        errorMessage = "Une erreur interne s'est produite";
                }

                return ResponseEntity
                                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(ApiResponse.error(errorMessage));
        }
}