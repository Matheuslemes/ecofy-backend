package br.com.ecofy.ms_notification.adapters.in.kafka.mapper;

import br.com.ecofy.ms_notification.adapters.in.kafka.dto.BudgetAlertEventMessage;
import br.com.ecofy.ms_notification.adapters.in.kafka.dto.InsightCreatedEventMessage;
import br.com.ecofy.ms_notification.core.application.command.HandleDomainEventCommand;
import br.com.ecofy.ms_notification.core.domain.enums.DomainEventType;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Component
public class InboundEventMapper {

    public HandleDomainEventCommand fromBudgetAlert(BudgetAlertEventMessage msg) {
        Objects.requireNonNull(msg, "msg must not be null");

        UUID userId = requireUserId(msg.userId());

        Map<String, Object> payload = new HashMap<>(8);
        putIfNotNull(payload, "budgetId", msg.budgetId());
        putIfNotNull(payload, "categoryId", msg.categoryId());
        putIfNotNull(payload, "limitAmount", msg.limitAmount());
        putIfNotNull(payload, "consumedAmount", msg.consumedAmount());
        putIfNotNull(payload, "consumedPct", msg.consumedPct());
        putIfNotNull(payload, "severity", msg.severity());

        String eventId = (msg.metadata() != null) ? msg.metadata().eventId() : null;

        return toCommand(DomainEventType.BUDGET_ALERT, userId, payload, eventId);
    }

    public HandleDomainEventCommand fromInsightCreated(InsightCreatedEventMessage msg) {
        Objects.requireNonNull(msg, "msg must not be null");

        UUID userId = requireUserId(msg.userId());

        Map<String, Object> payload = new HashMap<>(6);
        putIfNotNull(payload, "insightId", msg.insightId());
        putIfNotNull(payload, "insightType", msg.insightType());
        putIfNotNull(payload, "periodStart", msg.periodStart());
        putIfNotNull(payload, "periodEnd", msg.periodEnd());

        String eventId = (msg.metadata() != null) ? msg.metadata().eventId() : null;

        return toCommand(DomainEventType.INSIGHT_CREATED, userId, payload, eventId);
    }

    private static HandleDomainEventCommand toCommand(DomainEventType type,
                                                      UUID userId,
                                                      Map<String, Object> payload,
                                                      String eventId) {

        String idempotencyKey = resolveIdempotencyKey(eventId);
        return new HandleDomainEventCommand(type, userId, Map.copyOf(payload), idempotencyKey);
    }

    private static UUID requireUserId(UUID userId) {
        return Objects.requireNonNull(userId, "userId must not be null");
    }

    private static String resolveIdempotencyKey(String eventId) {
        return (eventId != null && !eventId.isBlank())
                ? eventId
                : UUID.randomUUID().toString();
    }

    private static void putIfNotNull(Map<String, Object> target, String key, Object value) {
        if (value != null) target.put(key, value);
    }
}
