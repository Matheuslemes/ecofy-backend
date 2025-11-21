package br.com.ecofy.auth.adapters.in.web.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(

        @NotBlank
        String clientId,

        String clientSecret,        // pode ser null em client public

        @NotBlank
        String username,

        @NotBlank
        String password,

        String scope

) { }