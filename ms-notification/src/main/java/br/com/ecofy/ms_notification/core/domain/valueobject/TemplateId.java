package br.com.ecofy.ms_notification.core.domain.valueobject;

import java.util.Objects;
import java.util.UUID;

public record TemplateId(UUID value) {
    public TemplateId {
        Objects.requireNonNull(value, "templateId must not be null");
    }
    public static TemplateId newId() { return new TemplateId(UUID.randomUUID()); }
}