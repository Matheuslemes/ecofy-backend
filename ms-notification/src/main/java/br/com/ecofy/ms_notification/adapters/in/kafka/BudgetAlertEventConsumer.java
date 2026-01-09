package br.com.ecofy.ms_notification.adapters.in.kafka;

import br.com.ecofy.ms_notification.adapters.in.kafka.dto.BudgetAlertEventMessage;
import br.com.ecofy.ms_notification.adapters.in.kafka.mapper.InboundEventMapper;
import br.com.ecofy.ms_notification.core.application.command.HandleDomainEventCommand;
import br.com.ecofy.ms_notification.core.port.in.HandleDomainEventNotificationUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class BudgetAlertEventConsumer {

    private final InboundEventMapper mapper;
    private final HandleDomainEventNotificationUseCase useCase;

    @KafkaListener(
            id = "budgetAlertEventConsumer",
            topics = "#{@notificationProperties.topics.budgetAlert}",
            containerFactory = "budgetAlertKafkaListenerContainerFactory"
    )
    public void consume(BudgetAlertEventMessage message) {
        Objects.requireNonNull(message, "message must not be null");

        String eventId = (message.metadata() != null) ? message.metadata().eventId() : null;
        var userId = message.userId();

        log.debug("[BudgetAlertEventConsumer] received budget.alert userId={} eventId={}", userId, eventId);

        try {
            HandleDomainEventCommand command = mapper.fromBudgetAlert(message);
            useCase.handle(command);

            log.debug("[BudgetAlertEventConsumer] processed budget.alert userId={} eventId={} idem={}",
                    userId, eventId, command.idempotencyKey());
        } catch (Exception ex) {
            // Re-throw para que o ErrorHandler do container (retry/backoff/DLT) faça o trabalho correto.
            log.error("[BudgetAlertEventConsumer] failed budget.alert userId={} eventId={}", userId, eventId, ex);
            throw ex;
        }
    }
}
