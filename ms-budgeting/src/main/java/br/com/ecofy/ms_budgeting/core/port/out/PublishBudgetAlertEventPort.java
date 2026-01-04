package br.com.ecofy.ms_budgeting.core.port.out;

import br.com.ecofy.ms_budgeting.core.domain.BudgetAlert;

public interface PublishBudgetAlertEventPort {

    void publish(BudgetAlert alert);

}
