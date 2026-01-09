package br.com.ecofy.ms_notification.adapters.out.persistence.mapper;

import br.com.ecofy.ms_notification.adapters.out.persistence.document.NotificationTemplateDocument;
import br.com.ecofy.ms_notification.core.domain.NotificationTemplate;
import br.com.ecofy.ms_notification.core.domain.valueobject.TemplateId;
import br.com.ecofy.ms_notification.core.domain.valueobject.UserId;
import org.springframework.stereotype.Component;

@Component
public class TemplateMapper {

    public NotificationTemplateDocument toDoc(NotificationTemplate t) {
        return NotificationTemplateDocument.builder()
                .id(t.getId().value())
                .ownerUserId(t.getOwnerUserId() == null ? null : t.getOwnerUserId().value())
                .eventType(t.getEventType())
                .channel(t.getChannel())
                .engine(t.getEngine())
                .subjectTemplate(t.getSubjectTemplate())
                .bodyTemplate(t.getBodyTemplate())
                .active(t.isActive())
                .createdAt(t.getCreatedAt())
                .updatedAt(t.getUpdatedAt())
                .build();
    }

    public NotificationTemplate toDomain(NotificationTemplateDocument d) {
        return NotificationTemplate.builder()
                .id(new TemplateId(d.getId()))
                .ownerUserId(d.getOwnerUserId() == null ? null : new UserId(d.getOwnerUserId()))
                .eventType(d.getEventType())
                .channel(d.getChannel())
                .engine(d.getEngine())
                .subjectTemplate(d.getSubjectTemplate())
                .bodyTemplate(d.getBodyTemplate())
                .active(d.isActive())
                .createdAt(d.getCreatedAt())
                .updatedAt(d.getUpdatedAt())
                .build();
    }
}
