package com.app.easypharma_backend.application.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokenRequest {

    @NotBlank(message = "Le refresh token est obligatoire")
    @Schema(description = "Jeton de rafraîchissement utilisé pour obtenir un nouveau jeton d'accès", 
            example = "dGhpcyBpcyBhIHJlZnJlc2ggdG9rZW4gdGhhdCBjYW4gYmUgdXNlZCB0byBnZXQgYSBuZXcgYWNjZXNzIHRva2Vu", 
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String refreshToken;
}