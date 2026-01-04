package br.com.ecofy.ms_budgeting.core.port.in;

import br.com.ecofy.ms_budgeting.core.application.result.BudgetOverviewResult;

import java.util.UUID;

public interface GetBudgetOverviewUseCase {

    BudgetOverviewResult overview(UUID userId);

}