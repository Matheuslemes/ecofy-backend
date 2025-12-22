package br.com.ecofy.ms_categorization.config;

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
            "/actuator/health",
            "/actuator/info",

            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html"
    };

    private static final String[] CATEGORIZATION_API_ENDPOINTS = {
            "/api/categorization/**"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        log.info("[SecurityConfig] - [securityFilterChain] -> Configurando HTTP security para ms-categorization");

        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                                .requestMatchers(PUBLIC_ENDPOINTS).permitAll()

                                // LOCAL DEV MODE (sem token) — DESCOMENTE para testes locais
                                .requestMatchers(CATEGORIZATION_API_ENDPOINTS).permitAll()

                                // PROD MODE (com token) — DEIXE ATIVO para exigir JWT
//                        .requestMatchers(CATEGORIZATION_API_ENDPOINTS).authenticated()

                                .anyRequest().authenticated()
                )
                // PROD MODE (com token) — DEIXE ATIVO para exigir JWT
                // (em local dev mode, pode comentar este bloco inteiro)
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
                .headers(headers -> headers
                        .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'"))
                        .frameOptions(frame -> frame.sameOrigin())
                );

        return http.build();
    }

}
