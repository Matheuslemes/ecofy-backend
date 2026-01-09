package br.com.ecofy.ms_notification.adapters.out.persistence.repository;

import br.com.ecofy.ms_notification.adapters.out.persistence.document.NotificationDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.UUID;

public interface NotificationMongoRepository extends MongoRepository<NotificationDocument, UUID> {
    List<NotificationDocument> findByUserIdOrderByCreatedAtDesc(UUID userId);
}