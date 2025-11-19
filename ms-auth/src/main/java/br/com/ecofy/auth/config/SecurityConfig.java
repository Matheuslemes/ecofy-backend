package br.com.ecofy.auth.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.web.SecurityFilterChain;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Configuration
@EnableMethodSecurity
@EnableConfigurationProperties(JwtProperties.class)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtProperties jwtProperties;
    private final ResourceLoader resourceLoader;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtDecoder jwtDecoder) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // Endpoints públicos
                        .requestMatchers(
                                "/actuator/health",
                                "/actuator/info",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/api/auth/token",
                                "/api/auth/refresh",
                                "/api/register/**",
                                "/api/password/**",
                                "/.well-known/jwks.json"
                        ).permitAll()
                        // demais endpoints precisam de autenticação
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.decoder(jwtDecoder))
                );

        // headers extras, HSTS, etc.
        http.headers(headers -> headers
                .contentSecurityPolicy(csp -> csp
                        .policyDirectives("default-src 'self'"))
                .frameOptions(frame -> frame.sameOrigin())
        );

        return http.build();
    }

    @Bean
    public JwtDecoder jwtDecoder() throws GeneralSecurityException, IOException {
        RSAPublicKey publicKey = loadPublicKey(jwtProperties.getPublicKeyLocation());
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withPublicKey(publicKey).build();

        decoder.setJwtValidator(new JwtValidatorFactory(jwtProperties).create());

        return decoder;

    }

    private RSAPublicKey loadPublicKey(String location) throws IOException, GeneralSecurityException {
        if (location == null || location.isBlank()) {
            throw new IllegalStateException("security.jwt.public-key-location must be configured");
        }

        Resource resource = resourceLoader.getResource(location);
        if (!resource.exists()) {
            throw new IllegalStateException("Public key resource not found: " + location);
        }

        try (InputStream is = resource.getInputStream()) {
            String pem = new String(is.readAllBytes());
            String sanitized = pem
                    .replace("BEGIN PUBLIC KEY", "")
                    .replace("END PUBLIC KEY", "")
                    .replaceAll("\\s", "");

            byte[] decoded = Base64.getDecoder().decode(sanitized);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decoded);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return (RSAPublicKey) keyFactory.generatePublic(keySpec);
        }
    }

    // fabrica simples para montar um validator customizado de JWT
    // (Issuer, clock skww etc.)
    // validacoes de audience, escopos, etc.
    static class JwtValidatorFactory {

        private final JwtProperties jwtProperties;

        JwtValidatorFactory(JwtProperties jwtProperties) {
            this.jwtProperties = jwtProperties;
        }

        public OAuth2TokenValidator<Jwt> create() {

            var validators = new java.util.ArrayList<OAuth2TokenValidator<org.springframework.security.oauth2.jwt.Jwt>>();

            if (jwtProperties.getIssuer() != null) {
                validators.add(new JwtIssuerValidator(jwtProperties.getIssuer()));
            }

            // validador default (timestamp, exp, nbf)
            validators.add(JwtValidators.createDefault());

            // combina todos em um
            return new DelegatingOAuth2TokenValidator<>(validators);

        }

    }

}
