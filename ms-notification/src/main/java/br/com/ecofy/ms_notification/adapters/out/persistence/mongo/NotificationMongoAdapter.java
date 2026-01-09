package br.com.ecofy.ms_notification.adapters.out.persistence.mongo;

import br.com.ecofy.ms_notification.adapters.out.persistence.mapper.NotificationMapper;
import br.com.ecofy.ms_notification.adapters.out.persistence.repository.NotificationMongoRepository;
import br.com.ecofy.ms_notification.core.application.result.NotificationResult;
import br.com.ecofy.ms_notification.core.domain.Notification;
import br.com.ecofy.ms_notification.core.domain.valueobject.NotificationId;
import br.com.ecofy.ms_notification.core.port.out.SaveNotificationPort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class NotificationMongoAdapter implements SaveNotificationPort {

    private final NotificationMongoRepository repo;
    private final NotificationMapper mapper;

    public NotificationMongoAdapter(NotificationMongoRepository repo, NotificationMapper mapper) {
        this.repo = repo;
        this.mapper = mapper;
    }

    @Override
    public Notification save(Notification notification) {
        var saved = repo.save(mapper.toDoc(notification));
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Notification> loadById(NotificationId id) {
        return repo.findById(id.value()).map(mapper::toDomain);
    }

    public List<NotificationResult> listByUser(UUID userId, int limit) {
        return repo.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .limit(limit)
                .map(d -> new NotificationResult(
                        d.getId(),
                        d.getUserId(),
                        d.getEventType(),
                        d.getChannel(),
                        d.getDestination(),
                        d.getSubject(),
                        d.getBody(),
                        d.getStatus(),
                        d.getAttemptCount(),
                        d.getPayload(),
                        d.getCreatedAt(),
                        d.getUpdatedAt()
                ))
                .toList();
    }
}