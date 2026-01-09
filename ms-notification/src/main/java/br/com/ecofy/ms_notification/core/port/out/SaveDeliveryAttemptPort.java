package br.com.ecofy.ms_notification.core.port.out;

import br.com.ecofy.ms_notification.core.domain.DeliveryAttempt;
import br.com.ecofy.ms_notification.core.domain.valueobject.NotificationId;

import java.util.List;

public interface SaveDeliveryAttemptPort {
    DeliveryAttempt save(DeliveryAttempt attempt);
    List<DeliveryAttempt> loadByNotificationId(NotificationId notificationId);
}