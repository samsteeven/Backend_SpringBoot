package com.app.easypharma_backend.application.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    @JsonProperty("access_token")
    @Schema(description = "Jeton d'accès JWT pour l'authentification", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String accessToken;

    @JsonProperty("refresh_token")
    @Schema(description = "Jeton de rafraîchissement utilisé pour obtenir un nouveau jeton d'accès", example = "dGhpcyBpcyBhIHJlZnJlc2ggdG9rZW4gdGhhdCBjYW4gYmUgdXNlZCB0byBnZXQgYSBuZXcgYWNjZXNzIHRva2Vu")
    private String refreshToken;

    @JsonProperty("token_type")
    @Builder.Default
    @Schema(description = "Type de jeton", example = "Bearer")
    private String tokenType = "Bearer";

    @JsonProperty("expires_in")
    @Schema(description = "Durée de validité du jeton d'accès en secondes", example = "86400")
    private Long expiresIn;

    private UserResponse user;
}