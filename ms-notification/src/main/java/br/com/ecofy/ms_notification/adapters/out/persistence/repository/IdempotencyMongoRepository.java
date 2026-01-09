package br.com.ecofy.ms_notification.adapters.out.persistence.repository;

import br.com.ecofy.ms_notification.adapters.out.persistence.document.IdempotencyKeyDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface IdempotencyMongoRepository extends MongoRepository<IdempotencyKeyDocument, String> {
    boolean existsByKey(String key);
}