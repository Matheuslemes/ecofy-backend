package br.com.ecofy.ms_notification.adapters.out.persistence.index;

import br.com.ecofy.ms_notification.adapters.out.persistence.document.NotificationDocument;
import jakarta.annotation.PostConstruct;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.stereotype.Component;

@Component
public class NotificationIndexes {

    private final MongoTemplate mongoTemplate;

    public NotificationIndexes(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @PostConstruct
    public void ensureIndexes() {

        mongoTemplate.indexOps(NotificationDocument.class).ensureIndex(
                new Index()
                        .on("userId", Sort.Direction.ASC)
                        .on("createdAt", Sort.Direction.DESC)
                        .named("idx_notifications_user_createdAt")
        );

        mongoTemplate.indexOps(NotificationDocument.class).ensureIndex(
                new Index()
                        .on("idempotencyKey", Sort.Direction.ASC)
                        .unique()
                        .sparse()
                        .named("ux_notifications_idempotencyKey")
        );
    }
}
