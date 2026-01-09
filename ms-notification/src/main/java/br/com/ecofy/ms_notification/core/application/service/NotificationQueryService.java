package br.com.ecofy.ms_notification.core.application.service;


import br.com.ecofy.ms_notification.adapters.out.persistence.mongo.NotificationMongoAdapter;
import br.com.ecofy.ms_notification.core.port.in.ListNotificationsUseCase;
import org.springframework.stereotype.Service;

import javax.management.remote.NotificationResult;
import java.util.List;
import java.util.UUID;

@Service
public class NotificationQueryService implements ListNotificationsUseCase {

    private final NotificationMongoAdapter notificationMongoAdapter;

    public NotificationQueryService(NotificationMongoAdapter notificationMongoAdapter) {
        this.notificationMongoAdapter = notificationMongoAdapter;
    }

    @Override
    public List<NotificationResult> listByUser(UUID userId, int limit) {
        return notificationMongoAdapter.listByUser(userId, limit);
    }
}
