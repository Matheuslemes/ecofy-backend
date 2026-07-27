package br.com.ecofy.ms_categorization.adapters.in.web;

import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class AuthenticatedUser {

    public UUID currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth instanceof JwtAuthenticationToken jwt) {
            String subject = jwt.getToken().getSubject();
            if (subject != null && !subject.isBlank()) {
                try {
                    return UUID.fromString(subject.trim());
                } catch (IllegalArgumentException ex) {
                    throw new AuthenticationCredentialsNotFoundException("JWT subject is not a valid user id");
                }
            }
        }

        throw new AuthenticationCredentialsNotFoundException("JWT authentication required for this resource");
    }
}
