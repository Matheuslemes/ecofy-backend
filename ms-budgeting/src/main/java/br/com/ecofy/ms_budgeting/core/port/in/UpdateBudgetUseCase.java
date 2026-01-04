package br.com.ecofy.ms_budgeting.core.port.in;

import br.com.ecofy.ms_budgeting.core.application.command.UpdateBudgetCommand;
import br.com.ecofy.ms_budgeting.core.application.result.BudgetResult;

public interface UpdateBudgetUseCase {

    BudgetResult update(UpdateBudgetCommand cmd, String idempotencyKey);

}