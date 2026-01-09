package br.com.ecofy.ms_notification.adapters.out.persistence.mapper;

import br.com.ecofy.ms_notification.adapters.out.persistence.document.DeliveryAttemptDocument;
import br.com.ecofy.ms_notification.core.domain.DeliveryAttempt;
import br.com.ecofy.ms_notification.core.domain.valueobject.NotificationId;
import org.springframework.stereotype.Component;

@Component
public class AttemptMapper {

    public DeliveryAttemptDocument toDoc(DeliveryAttempt a) {
        return DeliveryAttemptDocument.builder()
                .id(a.getId())
                .notificationId(a.getNotificationId().value())
                .channel(a.getChannel())
                .attemptNumber(a.getAttemptNumber())
                .status(a.getStatus())
                .provider(a.getProvider())
                .providerMessageId(a.getProviderMessageId())
                .errorCode(a.getErrorCode())
                .errorMessage(a.getErrorMessage())
                .createdAt(a.getCreatedAt())
                .build();
    }

    public DeliveryAttempt toDomain(DeliveryAttemptDocument d) {
        return DeliveryAttempt.builder()
                .id(d.getId())
                .notificationId(new NotificationId(d.getNotificationId()))
                .channel(d.getChannel())
                .attemptNumber(d.getAttemptNumber())
                .status(d.getStatus())
                .provider(d.getProvider())
                .providerMessageId(d.getProviderMessageId())
                .errorCode(d.getErrorCode())
                .errorMessage(d.getErrorMessage())
                .createdAt(d.getCreatedAt())
                .build();
    }
}