package br.com.ecofy.ms_budgeting.core.port.in;

import br.com.ecofy.ms_budgeting.core.application.command.CreateBudgetCommand;
import br.com.ecofy.ms_budgeting.core.application.result.BudgetResult;

public interface CreateBudgetUseCase {

    BudgetResult create(CreateBudgetCommand cmd, String idempotencyKey);

}