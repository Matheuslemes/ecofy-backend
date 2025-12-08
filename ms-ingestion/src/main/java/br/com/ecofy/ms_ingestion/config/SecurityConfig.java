package br.com.ecofy.ms_ingestion.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
@Slf4j
public class SecurityConfig {

    private static final String[] PUBLIC_ENDPOINTS = {
            // Actuator básicos
            "/actuator/health",
            "/actuator/info",

            // OpenAPI / Swagger
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html"
    };

    private static final String[] INGESTION_API_ENDPOINTS = {
            "/api/import/**"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        log.info("[SecurityConfig] - [securityFilterChain] -> Configurando HTTP security para ms-ingestion");

        http
                // ms-ingestion é protegido atrás do Gateway, não precisa de CSRF para APIs stateless
                .csrf(csrf -> csrf.disable())

                .authorizeHttpRequests(auth -> auth
                        // Endpoints públicos
                        .requestMatchers(PUBLIC_ENDPOINTS).permitAll()

                        // Endpoints de ingestão exigem JWT válido (emitido pelo ms-auth)
                        .requestMatchers(INGESTION_API_ENDPOINTS).authenticated()

                        // Qualquer outro endpoint também exige autenticação
                        .anyRequest().authenticated()
                )

                // Configura ms-ingestion como OAuth2 Resource Server (Bearer JWT)
                // A configuração de issuer-uri / jwk-set-uri fica no application.yml
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(Customizer.withDefaults())
                )

                // Cabeçalhos de segurança básicos
                .headers(headers -> headers
                        .contentSecurityPolicy(csp -> csp
                                .policyDirectives("default-src 'self'"))
                        .frameOptions(frame -> frame.sameOrigin())
                );

        return http.build();
    }
}