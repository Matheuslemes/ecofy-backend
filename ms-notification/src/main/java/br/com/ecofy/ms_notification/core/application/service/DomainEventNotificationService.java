package br.com.ecofy.ms_notification.core.application.service;

import br.com.ecofy.ms_notification.config.NotificationProperties;
import br.com.ecofy.ms_notification.core.application.command.HandleDomainEventCommand;
import br.com.ecofy.ms_notification.core.application.command.SendNotificationCommand;
import br.com.ecofy.ms_notification.core.domain.enums.DomainEventType;
import br.com.ecofy.ms_notification.core.domain.enums.NotificationChannel;
import br.com.ecofy.ms_notification.core.port.in.HandleDomainEventNotificationUseCase;
import br.com.ecofy.ms_notification.core.port.in.SendNotificationUseCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
public class DomainEventNotificationService implements HandleDomainEventNotificationUseCase {

    private final NotificationProperties props;
    private final SendNotificationUseCase sendNotificationUseCase;

    public DomainEventNotificationService(NotificationProperties props,
                                          SendNotificationUseCase sendNotificationUseCase) {
        this.props = Objects.requireNonNull(props, "props must not be null");
        this.sendNotificationUseCase = Objects.requireNonNull(sendNotificationUseCase, "sendNotificationUseCase must not be null");
    }

    @Override
    public void handle(HandleDomainEventCommand command) {

        UUID userId = Objects.requireNonNull(command.userId(), "userId must not be null");
        DomainEventType eventType = Objects.requireNonNull(command.eventType(), "eventType must not be null");
        Map<String, Object> payload = command.payload() == null ? Map.of() : command.payload();

        NotificationChannel channel = resolveDefaultChannel(eventType);

        log.debug("[DomainEventNotificationService] handle eventType={} userId={} channel={}",
                eventType, userId, channel);

        sendNotificationUseCase.send(new SendNotificationCommand(
                userId,
                eventType,
                channel,
                null,
                payload,
                command.idempotencyKey()
        ));
    }

    private NotificationChannel resolveDefaultChannel(DomainEventType type) {
        String raw = props.getTemplates().getDefaultChannels().getOrDefault(type.name(), "EMAIL");
        try {
            return NotificationChannel.valueOf(raw);
        } catch (Exception e) {
            return NotificationChannel.EMAIL;
        }
    }
}
