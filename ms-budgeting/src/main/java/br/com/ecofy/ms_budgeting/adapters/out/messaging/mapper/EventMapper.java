package br.com.ecofy.ms_budgeting.adapters.out.messaging.mapper;

import br.com.ecofy.ms_budgeting.adapters.out.messaging.dto.BudgetAlertEvent;
import br.com.ecofy.ms_budgeting.core.domain.BudgetAlert;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class EventMapper {

    private EventMapper() {}

    // Mantém compatibilidade com o seu código atual
    public static BudgetAlertEvent toEvent(BudgetAlert alert) {
        return toEvent(alert, Clock.systemUTC());
    }

    public static BudgetAlertEvent toEvent(BudgetAlert alert, Clock clock) {
        Objects.requireNonNull(alert, "alert must not be null");
        Objects.requireNonNull(clock, "clock must not be null");

        if (alert.getBudgetId() == null) {
            throw new IllegalArgumentException("alert.budgetId must not be null");
        }
        if (alert.getSeverity() == null) {
            throw new IllegalArgumentException("alert.severity must not be null");
        }

        String message = normalizeRequired(alert.getMessage(), "alert.message");

        return new BudgetAlertEvent(
                deterministicEventId(alert),
                Instant.now(clock),
                alert.getBudgetId(),
                alert.getConsumptionId(),
                alert.getSeverity(),
                message,
                alert.getPeriodStart(),
                alert.getPeriodEnd()
        );
    }

    private static String deterministicEventId(BudgetAlert alert) {
        // Se você quiser aleatório: return UUID.randomUUID().toString();
        String base = String.valueOf(alert.getBudgetId())
                + "|" + String.valueOf(alert.getConsumptionId())
                + "|" + String.valueOf(alert.getSeverity())
                + "|" + String.valueOf(alert.getPeriodStart())
                + "|" + String.valueOf(alert.getPeriodEnd());

        return UUID.nameUUIDFromBytes(base.getBytes(StandardCharsets.UTF_8)).toString();
    }

    private static String normalizeRequired(String value, String field) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return value.trim();
    }
}
