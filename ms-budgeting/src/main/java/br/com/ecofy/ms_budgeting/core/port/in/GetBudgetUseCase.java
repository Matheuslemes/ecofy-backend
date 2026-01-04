package br.com.ecofy.ms_budgeting.core.port.in;

import br.com.ecofy.ms_budgeting.core.application.result.BudgetResult;

import java.util.UUID;

public interface GetBudgetUseCase {

    BudgetResult get(UUID budgetId);

}