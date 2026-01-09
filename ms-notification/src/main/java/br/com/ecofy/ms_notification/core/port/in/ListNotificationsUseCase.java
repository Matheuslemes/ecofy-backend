package br.com.ecofy.ms_notification.core.port.in;

import javax.management.remote.NotificationResult;

public interface ListNotificationsUseCase {
    List<NotificationResult> listByUser(UUID userId, int limit);
}
