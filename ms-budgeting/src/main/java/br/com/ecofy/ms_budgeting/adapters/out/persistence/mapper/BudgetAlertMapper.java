package br.com.ecofy.ms_budgeting.adapters.out.persistence.mapper;

import br.com.ecofy.ms_budgeting.adapters.out.persistence.entity.BudgetAlertEntity;
import br.com.ecofy.ms_budgeting.core.domain.BudgetAlert;
import br.com.ecofy.ms_budgeting.core.domain.enums.AlertSeverity;
import br.com.ecofy.ms_budgeting.core.domain.valueobject.Money;

import java.time.Instant;
import java.util.UUID;

public final class BudgetAlertMapper {

    private BudgetAlertMapper() {}

    public static BudgetAlertEntity toEntity(BudgetAlert d) {
        return BudgetAlertEntity.builder()
                .id(d.getId())
                .budgetId(d.getBudgetId())
                .userId(d.getUserId())
                .categoryId(d.getCategoryId())
                .severity(d.getSeverity().name())
                .message(d.getMessage())
                .thresholdPercent(d.getThresholdPercent())
                .consumedCents(d.getConsumed().cents())
                .limitCents(d.getLimit().cents())
                .currency(d.getLimit().currency())
                .createdAt(d.getCreatedAt())
                .build();
    }

    public static BudgetAlert toDomain(BudgetAlertEntity e) {
        if (e == null) return null;

        Money consumed = new Money(e.getConsumedCents(), e.getCurrency());
        Money limit = new Money(e.getLimitCents(), e.getCurrency());

        return new BudgetAlert(
                e.getId(),
                e.getBudgetId(),
                e.getUserId(),
                e.getCategoryId(),
                AlertSeverity.valueOf(e.getSeverity()),
                e.getMessage(),
                e.getThresholdPercent(),
                consumed,
                limit,
                e.getCreatedAt() == null ? Instant.now() : e.getCreatedAt()
        );
    }

    public static BudgetAlert newAlert(
            UUID budgetId,
            UUID userId,
            UUID categoryId,
            AlertSeverity severity,
            String message,
            Integer thresholdPercent,
            Money consumed,
            Money limit
    ) {
        return new BudgetAlert(
                UUID.randomUUID(),
                budgetId,
                userId,
                categoryId,
                severity,
                message,
                thresholdPercent,
                consumed,
                limit,
                Instant.now()
        );
    }
}
