package br.com.ecofy.auth.adapters.out.security;

import br.com.ecofy.auth.core.domain.valueobject.PasswordHash;
import br.com.ecofy.auth.core.port.out.PasswordHashingPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Component
@Slf4j
public class BCryptPasswordHashingAdapter implements PasswordHashingPort {

    private final PasswordEncoder passwordEncoder;

    public BCryptPasswordHashingAdapter(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = Objects.requireNonNull(passwordEncoder, "passwordEncoder must not be null");
    }

    @Override
    public PasswordHash hash(String rawPassword) {
        Objects.requireNonNull(rawPassword, "rawPassword must not be null");
        if (rawPassword.isBlank()) {
            throw new IllegalArgumentException("rawPassword must not be blank");
        }

        log.debug("[BCryptPasswordHashingAdapter] - [hash] -> Gerando hash de senha (tamanho={})",
                rawPassword.length());

        String encoded = passwordEncoder.encode(rawPassword);

        // nunca logar o hash, apenas dizer que foi gerado
        log.debug("[BCryptPasswordHashingAdapter] - [hash] -> Hash gerado com sucesso");

        return new PasswordHash(encoded);
    }

    @Override
    public boolean matches(String rawPassword, PasswordHash hash) {
        Objects.requireNonNull(rawPassword, "rawPassword must not be null");
        Objects.requireNonNull(hash, "hash must not be null");

        log.debug("[BCryptPasswordHashingAdapter] - [matches] -> Verificando senha (tamanho={})",
                rawPassword.length());

        boolean matches = passwordEncoder.matches(rawPassword, hash.value());

        log.debug("[BCryptPasswordHashingAdapter] - [matches] -> Resultado matches={}", matches);

        return matches;
    }
}
