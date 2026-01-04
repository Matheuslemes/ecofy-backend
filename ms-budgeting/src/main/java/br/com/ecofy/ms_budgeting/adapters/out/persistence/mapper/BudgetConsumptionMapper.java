package br.com.ecofy.ms_budgeting.adapters.out.persistence.mapper;

import br.com.ecofy.ms_budgeting.adapters.out.persistence.entity.BudgetConsumptionEntity;
import br.com.ecofy.ms_budgeting.core.domain.BudgetConsumption;
import br.com.ecofy.ms_budgeting.core.domain.enums.ConsumptionSource;
import br.com.ecofy.ms_budgeting.core.domain.valueobject.Money;

import java.time.Instant;
import java.util.UUID;

public final class BudgetConsumptionMapper {

    private BudgetConsumptionMapper() {}

    public static BudgetConsumptionEntity toEntity(BudgetConsumption d) {
        return BudgetConsumptionEntity.builder()
                .id(d.getId())
                .budgetId(d.getBudgetId())
                .consumedCents(d.getConsumed().cents())
                .currency(d.getConsumed().currency())
                .source(d.getSource().name())
                .lastTransactionId(d.getLastTransactionId())
                .updatedAt(d.getUpdatedAt())
                .build();
    }

    public static BudgetConsumption toDomain(BudgetConsumptionEntity e) {
        if (e == null) return null;
        return new BudgetConsumption(
                e.getId(),
                e.getBudgetId(),
                new Money(e.getConsumedCents(), e.getCurrency()),
                ConsumptionSource.valueOf(e.getSource()),
                e.getLastTransactionId(),
                e.getUpdatedAt() == null ? Instant.now() : e.getUpdatedAt()
        );
    }

    public static BudgetConsumption newEmpty(UUID budgetId, String currency) {
        return new BudgetConsumption(
                UUID.randomUUID(),
                budgetId,
                new Money(0L, currency),
                ConsumptionSource.CATEGORIZED_TX,
                null,
                Instant.now()
        );
    }
}
