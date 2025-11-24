package br.com.ecofy.auth.adapters.out.verification;

import br.com.ecofy.auth.core.domain.AuthUser;
import br.com.ecofy.auth.core.port.out.VerificationTokenStorePort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementação em memória da store de tokens de verificação de e-mail.
 * ÚTIL PARA DEV / TESTES.

 * Em produção, substitua por Redis/JPA/etc.
 */
@Slf4j
@Component
public class InMemoryVerificationTokenStoreAdapter implements VerificationTokenStorePort {

    /**
     * token -> entry(user, createdAt)
     */
    private final Map<String, Entry> tokens = new ConcurrentHashMap<>();

    @Override
    public void store(AuthUser user, String token) {
        if (user == null || token == null || token.isBlank()) {
            log.warn("[InMemoryVerificationTokenStoreAdapter] - [store] -> user ou token nulos/blank");
            return;
        }

        tokens.put(token, new Entry(user, Instant.now()));

        log.debug(
                "[InMemoryVerificationTokenStoreAdapter] - [store] -> Token de verificação armazenado userId={} tokenMask={}",
                user.id().value(), maskToken(token)
        );
    }

    @Override
    public Optional<AuthUser> consume(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }

        Entry entry = tokens.remove(token);
        if (entry == null) {
            log.debug(
                    "[InMemoryVerificationTokenStoreAdapter] - [consume] -> Token não encontrado tokenMask={}",
                    maskToken(token)
            );
            return Optional.empty();
        }

        log.debug(
                "[InMemoryVerificationTokenStoreAdapter] - [consume] -> Token consumido com sucesso userId={} tokenMask={}",
                entry.user.id().value(), maskToken(token)
        );

        return Optional.of(entry.user);
    }

    // Tipos auxiliares

    private record Entry(AuthUser user, Instant createdAt) { }

    private String maskToken(String token) {
        if (token == null || token.isBlank()) return "***";
        return token.length() > 10 ? token.substring(0, 10) + "..." : "***";
    }
}
