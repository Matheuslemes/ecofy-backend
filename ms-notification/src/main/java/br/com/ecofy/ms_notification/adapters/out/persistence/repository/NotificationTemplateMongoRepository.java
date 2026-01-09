package br.com.ecofy.ms_notification.adapters.out.persistence.repository;

import br.com.ecofy.ms_notification.adapters.out.persistence.document.NotificationTemplateDocument;
import br.com.ecofy.ms_notification.core.domain.enums.DomainEventType;
import br.com.ecofy.ms_notification.core.domain.enums.NotificationChannel;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;
import java.util.UUID;

public interface NotificationTemplateMongoRepository extends MongoRepository<NotificationTemplateDocument, UUID> {

    Optional<NotificationTemplateDocument> findFirstByOwnerUserIdAndEventTypeAndChannelAndActiveTrue(
            UUID ownerUserId,
            DomainEventType eventType,
            NotificationChannel channel
    );

    Optional<NotificationTemplateDocument> findFirstByOwnerUserIdIsNullAndEventTypeAndChannelAndActiveTrue(
            DomainEventType eventType,
            NotificationChannel channel
    );
}

