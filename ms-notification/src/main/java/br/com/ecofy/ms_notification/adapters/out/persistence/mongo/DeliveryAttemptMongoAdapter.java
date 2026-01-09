package br.com.ecofy.ms_notification.adapters.out.persistence.mongo;

import br.com.ecofy.ms_notification.adapters.out.persistence.mapper.AttemptMapper;
import br.com.ecofy.ms_notification.adapters.out.persistence.repository.DeliveryAttemptMongoRepository;
import br.com.ecofy.ms_notification.core.domain.DeliveryAttempt;
import br.com.ecofy.ms_notification.core.domain.valueobject.NotificationId;
import br.com.ecofy.ms_notification.core.port.out.SaveDeliveryAttemptPort;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DeliveryAttemptMongoAdapter implements SaveDeliveryAttemptPort {

    private final DeliveryAttemptMongoRepository repo;
    private final AttemptMapper mapper;

    public DeliveryAttemptMongoAdapter(DeliveryAttemptMongoRepository repo, AttemptMapper mapper) {
        this.repo = repo;
        this.mapper = mapper;
    }

    @Override
    public DeliveryAttempt save(DeliveryAttempt attempt) {
        var saved = repo.save(mapper.toDoc(attempt));
        return mapper.toDomain(saved);
    }

    @Override
    public List<DeliveryAttempt> loadByNotificationId(NotificationId notificationId) {
        return repo.findByNotificationIdOrderByAttemptNumberAsc(notificationId.value())
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
}