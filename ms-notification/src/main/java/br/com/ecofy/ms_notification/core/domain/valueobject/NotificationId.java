package br.com.ecofy.ms_notification.core.domain.valueobject;

import java.util.Objects;
import java.util.UUID;

public record NotificationId(UUID value) {
    public NotificationId {
        Objects.requireNonNull(value, "notificationId must not be null");
    }
    public static NotificationId newId() { return new NotificationId(UUID.randomUUID()); }
}