package br.com.ecofy.ms_notification.core.port.out;

import br.com.ecofy.ms_notification.core.domain.NotificationTemplate;
import br.com.ecofy.ms_notification.core.domain.enums.DomainEventType;
import br.com.ecofy.ms_notification.core.domain.enums.NotificationChannel;
import br.com.ecofy.ms_notification.core.domain.valueobject.TemplateId;
import br.com.ecofy.ms_notification.core.domain.valueobject.UserId;

import java.util.Optional;

public interface LoadNotificationTemplatePort {

    Optional<NotificationTemplate> loadById(TemplateId id);

    Optional<NotificationTemplate> loadActiveTemplate(UserId userIdOrNull,
                                                      DomainEventType eventType,
                                                      NotificationChannel channel);
}