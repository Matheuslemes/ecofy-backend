package br.com.ecofy.ms_insights.core.port.out;

import java.util.List;
import java.util.UUID;

public interface LoadBudgetsForUserPort {
    List<BudgetView> loadBudgets(UUID userId);

    record BudgetView(UUID budgetId, UUID categoryId, long limitCents, String currency, String status) {}
}
