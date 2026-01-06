package br.com.ecofy.ms_insights.adapters.out.persistence;

import br.com.ecofy.ms_insights.adapters.out.persistence.entity.IdempotencyKeyEntity;
import br.com.ecofy.ms_insights.adapters.out.persistence.repository.IdempotencyRepository;
import br.com.ecofy.ms_insights.core.port.out.IdempotencyPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

@Slf4j
@Component
public class IdempotencyJpaAdapter implements IdempotencyPort {

    private final IdempotencyRepository repository;
    private final Clock clock;

    public IdempotencyJpaAdapter(IdempotencyRepository repository, Clock clock) {
        this.repository = Objects.requireNonNull(repository, "repository must not be null");
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
    }

    @Override
    @Transactional
    public boolean tryAcquire(String key, int ttlSeconds) {
        String k = requireNonBlank(key, "key");

        if (ttlSeconds <= 0) {
            throw new IllegalArgumentException("ttlSeconds must be positive");
        }

        Instant now = Instant.now(clock);

        try {
            repository.save(IdempotencyKeyEntity.builder()
                    .key(k)
                    .createdAt(now)
                    .expiresAt(now.plusSeconds(ttlSeconds))
                    .build());

            log.debug("[IdempotencyJpaAdapter] - [tryAcquire] -> ACQUIRED key={} ttlSeconds={}", k, ttlSeconds);
            return true;

        } catch (DataIntegrityViolationException ex) {
            boolean reacquired = tryReacquireIfExpired(k, ttlSeconds, now);

            if (!reacquired) {
                log.debug("[IdempotencyJpaAdapter] - [tryAcquire] -> REJECTED key={}", k);
            }

            return reacquired;
        }
    }

    private boolean tryReacquireIfExpired(String key, int ttlSeconds, Instant now) {
        return repository.findById(key)
                .filter(existing -> existing.getExpiresAt() != null && existing.getExpiresAt().isBefore(now))
                .map(existing -> {
                    repository.deleteById(key);

                    try {
                        repository.save(IdempotencyKeyEntity.builder()
                                .key(key)
                                .createdAt(now)
                                .expiresAt(now.plusSeconds(ttlSeconds))
                                .build());

                        log.debug("[IdempotencyJpaAdapter] - [tryAcquire] -> REACQUIRED key={} ttlSeconds={}", key, ttlSeconds);
                        return true;

                    } catch (DataIntegrityViolationException ignoreRace) {
                        log.debug("[IdempotencyJpaAdapter] - [tryAcquire] -> REACQUIRE_FAILED_RACE key={}", key);
                        return false;
                    }
                })
                .orElse(false);
    }

    private static String requireNonBlank(String v, String field) {
        if (v == null || v.trim().isEmpty()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return v.trim();
    }
}
