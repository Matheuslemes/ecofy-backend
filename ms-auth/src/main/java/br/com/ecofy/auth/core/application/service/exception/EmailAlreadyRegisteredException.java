package br.com.ecofy.auth.core.application.service.exception;

import java.util.UUID;

public class EmailAlreadyRegisteredException extends RuntimeException {

    private final String email;
    private final UUID userId;

    public EmailAlreadyRegisteredException(String email, UUID userId) {
        super("Email already registered: " + email);
        this.email = email;
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public UUID getUserId() {
        return userId;
    }

}
