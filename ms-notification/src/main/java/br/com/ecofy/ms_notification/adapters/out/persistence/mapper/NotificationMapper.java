package br.com.ecofy.ms_notification.adapters.out.persistence.mapper;

import br.com.ecofy.ms_notification.adapters.out.persistence.document.NotificationDocument;
import br.com.ecofy.ms_notification.core.domain.Notification;
import br.com.ecofy.ms_notification.core.domain.valueobject.ChannelAddress;
import br.com.ecofy.ms_notification.core.domain.valueobject.IdempotencyKey;
import br.com.ecofy.ms_notification.core.domain.valueobject.NotificationId;
import br.com.ecofy.ms_notification.core.domain.valueobject.UserId;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {

    public NotificationDocument toDoc(Notification n) {
        return NotificationDocument.builder()
                .id(n.getId().value())
                .userId(n.getUserId().value())
                .eventType(n.getEventType())
                .channel(n.getChannel())
                .destination(n.getDestination().address())
                .subject(n.getSubject())
                .body(n.getBody())
                .status(n.getStatus())
                .attemptCount(n.getAttemptCount())
                .idempotencyKey(n.getIdempotencyKey() == null ? null : n.getIdempotencyKey().value())
                .payload(n.getPayload())
                .createdAt(n.getCreatedAt())
                .updatedAt(n.getUpdatedAt())
                .build();
    }

    public Notification toDomain(NotificationDocument d) {
        return Notification.builder()
                .id(new NotificationId(d.getId()))
                .userId(new UserId(d.getUserId()))
                .eventType(d.getEventType())
                .channel(d.getChannel())
                .destination(new ChannelAddress(d.getChannel(), d.getDestination()))
                .subject(d.getSubject())
                .body(d.getBody())
                .status(d.getStatus())
                .attemptCount(d.getAttemptCount())
                .idempotencyKey(d.getIdempotencyKey() == null ? null : new IdempotencyKey(d.getIdempotencyKey()))
                .payload(d.getPayload())
                .createdAt(d.getCreatedAt())
                .updatedAt(d.getUpdatedAt())
                .build();
    }
}