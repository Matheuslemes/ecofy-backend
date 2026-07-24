package br.com.ecofy.ms_budgeting.adapters.in.web.dto.response;

import br.com.ecofy.ms_budgeting.core.application.result.BudgetOverviewResult;

import java.util.List;
import java.util.UUID;

public record BudgetOverviewResponse(

        UUID userId,

        List<BudgetConsumptionResponse> consumptions,

        List<?> alerts

) {

    // converte o resultado da aplicação (use case) para o DTO de resposta da API
    public static BudgetOverviewResponse from(BudgetOverviewResult r) {
        List<BudgetConsumptionResponse> consumptions = r.consumptions().stream()
                .map(BudgetConsumptionResponse::from)
                .toList();
        return new BudgetOverviewResponse(r.userId(), consumptions, r.alerts());
    }

}
