package br.com.ecofy.ms_notification.adapters.out.persistence.index;

import br.com.ecofy.ms_notification.adapters.out.persistence.document.IdempotencyKeyDocument;
import jakarta.annotation.PostConstruct;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class IdempotencyIndexes {

    private final MongoTemplate mongoTemplate;

    public IdempotencyIndexes(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @PostConstruct
    public void ensureIndexes() {

        mongoTemplate.indexOps(IdempotencyKeyDocument.class).ensureIndex(
                new Index()
                        .on("expiresAt", org.springframework.data.domain.Sort.Direction.ASC)
                        .expire(0, TimeUnit.SECONDS)
                        .named("ttl_idempotency_expiresAt")
        );
    }
}
