package br.com.ecofy.auth.adapters.in.web.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request para revogação de tokens.
 * Hoje o foco é refresh_token, mas já deixamos flexível.
 */
public record RevokeTokenRequest(

        @NotBlank
        String token,

        Boolean refreshToken

) { }
