package br.com.ecofy.ms_notification.adapters.out.persistence.repository;

import br.com.ecofy.ms_notification.adapters.out.persistence.document.DeliveryAttemptDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.UUID;

public interface DeliveryAttemptMongoRepository extends MongoRepository<DeliveryAttemptDocument, UUID> {
    List<DeliveryAttemptDocument> findByNotificationIdOrderByAttemptNumberAsc(UUID notificationId);
}
