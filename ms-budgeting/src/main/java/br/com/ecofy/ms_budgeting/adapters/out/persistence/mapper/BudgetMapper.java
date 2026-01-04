package br.com.ecofy.ms_budgeting.adapters.out.persistence.mapper;

import br.com.ecofy.ms_budgeting.adapters.out.persistence.entity.BudgetEntity;
import br.com.ecofy.ms_budgeting.core.domain.Budget;
import br.com.ecofy.ms_budgeting.core.domain.valueobject.*;

import java.util.Currency;

public final class BudgetMapper {

    private BudgetMapper() {}

    public static Budget toDomain(BudgetEntity e) {
        var key = new BudgetKey(
                new UserId(e.getUserId()),
                new CategoryId(e.getCategoryId()),
                new Period(e.getPeriodStart(), e.getPeriodEnd())
        );

        return new Budget(
                e.getId(),
                key,
                e.getPeriodType(),
                new Money(e.getLimitAmount(), Currency.getInstance(e.getCurrency())),
                e.getStatus(),
                e.getCreatedAt(),
                e.getUpdatedAt()
        );
    }

    public static BudgetEntity toEntity(Budget b) {
        return BudgetEntity.builder()
                .id(b.getId())
                .userId(b.getKey().userId().value())
                .categoryId(b.getKey().categoryId().value())
                .periodType(b.getPeriodType())
                .periodStart(b.getKey().period().start())
                .periodEnd(b.getKey().period().end())
                .limitAmount(b.getLimit().amount())
                .currency(b.getLimit().currency().getCurrencyCode())
                .status(b.getStatus())
                .naturalKey(b.getKey().asNaturalKey())
                .createdAt(b.getCreatedAt())
                .updatedAt(b.getUpdatedAt())
                .build();
    }

}
