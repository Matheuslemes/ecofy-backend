package br.com.ecofy.ms_notification.adapters.out.persistence.mongo;

import br.com.ecofy.ms_notification.adapters.out.persistence.mapper.TemplateMapper;
import br.com.ecofy.ms_notification.adapters.out.persistence.repository.NotificationTemplateMongoRepository;
import br.com.ecofy.ms_notification.core.domain.NotificationTemplate;
import br.com.ecofy.ms_notification.core.domain.enums.DomainEventType;
import br.com.ecofy.ms_notification.core.domain.enums.NotificationChannel;
import br.com.ecofy.ms_notification.core.domain.valueobject.TemplateId;
import br.com.ecofy.ms_notification.core.domain.valueobject.UserId;
import br.com.ecofy.ms_notification.core.port.out.LoadNotificationTemplatePort;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class NotificationTemplateMongoAdapter implements LoadNotificationTemplatePort {

    private final NotificationTemplateMongoRepository repo;
    private final TemplateMapper mapper;

    public NotificationTemplateMongoAdapter(NotificationTemplateMongoRepository repo, TemplateMapper mapper) {
        this.repo = repo;
        this.mapper = mapper;
    }

    public NotificationTemplate save(NotificationTemplate template) {
        var saved = repo.save(mapper.toDoc(template));
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<NotificationTemplate> loadById(TemplateId id) {
        return repo.findById(id.value()).map(mapper::toDomain);
    }

    @Override
    public Optional<NotificationTemplate> loadActiveTemplate(UserId userIdOrNull,
                                                             DomainEventType eventType,
                                                             NotificationChannel channel) {

        if (userIdOrNull != null) {
            var user = repo.findFirstByOwnerUserIdAndEventTypeAndChannelAndActiveTrue(
                    userIdOrNull.value(), eventType, channel
            );
            if (user.isPresent()) return user.map(mapper::toDomain);
        }

        return repo.findFirstByOwnerUserIdIsNullAndEventTypeAndChannelAndActiveTrue(eventType, channel)
                .map(mapper::toDomain);
    }
}