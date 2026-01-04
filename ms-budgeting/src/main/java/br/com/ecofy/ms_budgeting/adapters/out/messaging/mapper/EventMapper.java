package br.com.ecofy.ms_budgeting.adapters.out.messaging.mapper;

import br.com.ecofy.ms_budgeting.adapters.out.messaging.dto.BudgetAlertEvent;
import br.com.ecofy.ms_budgeting.core.domain.BudgetAlert;

import java.time.Instant;
import java.util.UUID;

public final class EventMapper {

    private EventMapper() {}

    public static BudgetAlertEvent toEvent(BudgetAlert alert) {
        return new BudgetAlertEvent(
                UUID.randomUUID().toString(),
                Instant.now(),
                alert.getBudgetId(),
                alert.getConsumptionId(),
                alert.getSeverity(),
                alert.getMessage(),
                alert.getPeriodStart(),
                alert.getPeriodEnd()
        );
    }

}
