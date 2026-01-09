package br.com.ecofy.ms_notification.adapters.out.messaging.mapper;

import br.com.ecofy.ms_notification.adapters.out.messaging.dto.NotificationSentEvent;
import br.com.ecofy.ms_notification.core.domain.Notification;
import org.springframework.stereotype.Component;

@Component
public class EventMapper {
    public NotificationSentEvent toSentEvent(Notification notification) {
        return NotificationSentEvent.from(notification);
    }
}
