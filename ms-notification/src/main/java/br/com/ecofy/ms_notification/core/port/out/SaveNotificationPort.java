package br.com.ecofy.ms_notification.core.port.out;

import br.com.ecofy.ms_notification.core.domain.Notification;
import br.com.ecofy.ms_notification.core.domain.valueobject.NotificationId;

import java.util.Optional;

public interface SaveNotificationPort {
    Notification save(Notification notification);
    Optional<Notification> loadById(NotificationId id);
}
