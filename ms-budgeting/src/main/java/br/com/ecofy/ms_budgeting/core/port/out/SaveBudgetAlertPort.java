package br.com.ecofy.ms_budgeting.core.port.out;

import br.com.ecofy.ms_budgeting.core.domain.BudgetAlert;

public interface SaveBudgetAlertPort {

    BudgetAlert save(BudgetAlert alert);

}
