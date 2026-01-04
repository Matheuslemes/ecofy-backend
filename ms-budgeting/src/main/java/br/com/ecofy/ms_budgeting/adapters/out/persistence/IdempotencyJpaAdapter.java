package br.com.ecofy.ms_budgeting.adapters.out.persistence;


import br.com.ecofy.ms_budgeting.adapters.out.persistence.entity.IdempotencyKeyEntity;
import br.com.ecofy.ms_budgeting.adapters.out.persistence.repository.IdempotencyRepository;
import br.com.ecofy.ms_budgeting.core.port.out.IdempotencyPort;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

@Component
public class IdempotencyJpaAdapter implements IdempotencyPort {

    private final IdempotencyRepository repo;

    public IdempotencyJpaAdapter(IdempotencyRepository repo) {
        this.repo = repo;
    }

    @Override
    public boolean tryAcquire(String key, Duration ttl, String scope) {
        try {
            repo.save(IdempotencyKeyEntity.builder()
                    .key(key)
                    .scope(scope)
                    .createdAt(Instant.now())
                    .expiresAt(Instant.now().plus(ttl))
                    .build());
            return true;
        } catch (DataIntegrityViolationException e) {
            return false;
        }
    }

}