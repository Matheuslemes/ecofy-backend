package br.com.ecofy.ms_budgeting.core.domain;

import br.com.ecofy.ms_budgeting.core.domain.enums.AlertSeverity;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public class BudgetAlert {

    private final UUID id;
    private final UUID budgetId;
    private final UUID consumptionId;
    private final AlertSeverity severity;
    private final String message;
    private final LocalDate periodStart;
    private final LocalDate periodEnd;
    private final Instant createdAt;

    public BudgetAlert(UUID id,
                       UUID budgetId,
                       UUID consumptionId,
                       AlertSeverity severity,
                       String message,
                       LocalDate periodStart,
                       LocalDate periodEnd,
                       Instant createdAt) {

        this.id = Objects.requireNonNull(id);
        this.budgetId = Objects.requireNonNull(budgetId);
        this.consumptionId = Objects.requireNonNull(consumptionId);
        this.severity = Objects.requireNonNull(severity);
        this.message = Objects.requireNonNull(message);
        this.periodStart = Objects.requireNonNull(periodStart);
        this.periodEnd = Objects.requireNonNull(periodEnd);
        this.createdAt = Objects.requireNonNull(createdAt);
    }

    public UUID getId() { return id; }
    public UUID getBudgetId() { return budgetId; }
    public UUID getConsumptionId() { return consumptionId; }
    public AlertSeverity getSeverity() { return severity; }
    public String getMessage() { return message; }
    public LocalDate getPeriodStart() { return periodStart; }
    public LocalDate getPeriodEnd() { return periodEnd; }
    public Instant getCreatedAt() { return createdAt; }

}