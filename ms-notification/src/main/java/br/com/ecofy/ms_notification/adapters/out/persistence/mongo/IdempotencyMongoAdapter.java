package br.com.ecofy.ms_notification.adapters.out.persistence.mongo;

import br.com.ecofy.ms_notification.adapters.out.persistence.document.IdempotencyKeyDocument;
import br.com.ecofy.ms_notification.adapters.out.persistence.repository.IdempotencyMongoRepository;
import br.com.ecofy.ms_notification.config.NotificationProperties;
import br.com.ecofy.ms_notification.core.domain.valueobject.IdempotencyKey;
import br.com.ecofy.ms_notification.core.port.out.IdempotencyPort;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class IdempotencyMongoAdapter implements IdempotencyPort {

    private final IdempotencyMongoRepository repo;
    private final NotificationProperties props;

    public IdempotencyMongoAdapter(IdempotencyMongoRepository repo, NotificationProperties props) {
        this.repo = repo;
        this.props = props;
    }

    @Override
    public boolean tryAcquire(IdempotencyKey key) {

        if (repo.existsById(key.value())) {
            return false;
        }

        var now = Instant.now();
        var doc = IdempotencyKeyDocument.builder()
                .key(key.value())
                .createdAt(now)
                .expiresAt(now.plus(props.getIdempotency().getTtl()))
                .build();

        repo.save(doc);
        return true;
    }
}