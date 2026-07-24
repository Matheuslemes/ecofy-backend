package br.com.ecofy.ms_budgeting.adapters.in.web.dto.request;

import br.com.ecofy.ms_budgeting.core.domain.enums.BudgetStatus;
import jakarta.validation.constraints.Min;

// Representa os dados permitidos para atualização de um orçamento.
public record UpdateBudgetRequest(

        @Min(value = 1)
        Long newLimitAmountCents,

        String currency,

        BudgetStatus status,

        Long version

) {
    public UpdateBudgetRequest(Long newLimitAmountCents, String currency, BudgetStatus status) {
        this(newLimitAmountCents, currency, status, null);
    }
}
