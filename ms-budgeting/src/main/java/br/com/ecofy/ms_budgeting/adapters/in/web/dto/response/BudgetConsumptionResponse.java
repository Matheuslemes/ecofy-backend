package br.com.ecofy.ms_budgeting.adapters.in.web.dto.response;

import br.com.ecofy.ms_budgeting.adapters.in.web.support.MoneyCents;
import br.com.ecofy.ms_budgeting.core.application.result.BudgetConsumptionResult;

import java.math.BigDecimal;
import java.util.UUID;

// Consumo do orçamento no contrato da API em centavos inteiros (antes o
// overview vazava BudgetConsumptionResult com BigDecimal decimal). Alinha com insights/goals.
public record BudgetConsumptionResponse(

        UUID budgetId,

        long consumedCents,

        long limitCents,

        // Percentual de consumo (0..100+) — não é valor monetário, permanece decimal.
        BigDecimal consumedPct

) {

    public static BudgetConsumptionResponse from(BudgetConsumptionResult r) {
        return new BudgetConsumptionResponse(
                r.budgetId(),
                MoneyCents.toCents(r.consumedAmount()),
                MoneyCents.toCents(r.limitAmount()),
                r.consumedPct()
        );
    }
}
