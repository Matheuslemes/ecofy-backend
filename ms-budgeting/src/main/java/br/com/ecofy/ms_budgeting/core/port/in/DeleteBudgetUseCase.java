package br.com.ecofy.ms_budgeting.core.port.in;

import br.com.ecofy.ms_budgeting.core.application.command.DeleteBudgetCommand;

public interface DeleteBudgetUseCase {

    void delete(DeleteBudgetCommand cmd, String idempotencyKey);

}